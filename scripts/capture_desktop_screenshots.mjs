import { spawn } from 'node:child_process';
import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const shotDir = path.join(root, 'docs', '交付文档', '第4回_查询可视化与系统测试_交付包', 'screenshots');
const chromePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const frontend = 'http://127.0.0.1:5179';
const backend = 'http://127.0.0.1:8080/api';
const port = 9333;
const viewport = { width: 1440, height: 980, deviceScaleFactor: 1, mobile: false };

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitForJson(url, timeoutMs = 15000) {
  const deadline = Date.now() + timeoutMs;
  let lastError;
  while (Date.now() < deadline) {
    try {
      const response = await fetch(url);
      if (response.ok) return await response.json();
      lastError = new Error(`${response.status} ${response.statusText}`);
    } catch (error) {
      lastError = error;
    }
    await sleep(250);
  }
  throw lastError || new Error(`Timed out waiting for ${url}`);
}

class CdpClient {
  constructor(ws) {
    this.ws = ws;
    this.nextId = 1;
    this.pending = new Map();
    this.events = [];
    ws.addEventListener('message', (event) => {
      const message = JSON.parse(event.data);
      if (message.id && this.pending.has(message.id)) {
        const { resolve, reject } = this.pending.get(message.id);
        this.pending.delete(message.id);
        if (message.error) reject(new Error(message.error.message));
        else resolve(message.result);
      } else if (message.method) {
        this.events.push(message.method);
      }
    });
  }

  send(method, params = {}) {
    const id = this.nextId++;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => {
      this.pending.set(id, { resolve, reject });
    });
  }
}

async function connect(wsUrl) {
  const ws = new WebSocket(wsUrl);
  await new Promise((resolve, reject) => {
    ws.addEventListener('open', resolve, { once: true });
    ws.addEventListener('error', reject, { once: true });
  });
  return new CdpClient(ws);
}

async function waitForAppText(client, text, timeoutMs = 12000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const result = await client.send('Runtime.evaluate', {
      expression: `document.body && document.body.innerText.includes(${JSON.stringify(text)})`,
      returnByValue: true,
    });
    if (result.result.value === true) return;
    await sleep(250);
  }
  throw new Error(`Timed out waiting for page text: ${text}`);
}

async function capture(client, route, filename, marker) {
  await client.send('Page.navigate', { url: `${frontend}${route}` });
  await sleep(1800);
  if (marker) await waitForAppText(client, marker);
  await client.send('Runtime.evaluate', {
    expression: 'window.scrollTo(0, 0); document.documentElement.style.scrollBehavior = "auto";',
  });
  await sleep(300);
  const screenshot = await client.send('Page.captureScreenshot', {
    format: 'png',
    fromSurface: true,
    captureBeyondViewport: false,
  });
  await writeFile(path.join(shotDir, filename), Buffer.from(screenshot.data, 'base64'));
}

const profileDir = await mkdtemp(path.join(tmpdir(), 'hrms-chrome-profile-'));
const chrome = spawn(chromePath, [
  '--headless=new',
  '--disable-gpu',
  '--disable-dev-shm-usage',
  '--no-first-run',
  '--no-default-browser-check',
  `--remote-debugging-port=${port}`,
  `--user-data-dir=${profileDir}`,
  `--window-size=${viewport.width},${viewport.height}`,
  'about:blank',
], { stdio: 'ignore' });

try {
  await mkdir(shotDir, { recursive: true });
  const targets = await waitForJson(`http://127.0.0.1:${port}/json/list`);
  const page = targets.find((target) => target.type === 'page') || targets[0];
  const client = await connect(page.webSocketDebuggerUrl);
  await client.send('Page.enable');
  await client.send('Runtime.enable');
  await client.send('Emulation.setDeviceMetricsOverride', viewport);

  const loginResponse = await fetch(`${backend}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' }),
  });
  const loginBody = await loginResponse.json();
  if (!loginResponse.ok || loginBody.success === false) {
    throw new Error(`Login failed: ${loginBody.message || loginResponse.statusText}`);
  }
  const user = {
    username: loginBody.data.username,
    name: loginBody.data.name,
    role: loginBody.data.role,
  };
  const managerResponse = await fetch(`${backend}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'manager', password: 'manager123' }),
  });
  const managerBody = await managerResponse.json();
  if (!managerResponse.ok || managerBody.success === false) {
    throw new Error(`Manager login failed: ${managerBody.message || managerResponse.statusText}`);
  }
  const managerUser = {
    username: managerBody.data.username,
    name: managerBody.data.name,
    role: managerBody.data.role,
  };

  await client.send('Page.navigate', { url: frontend });
  await sleep(1000);
  await client.send('Runtime.evaluate', {
    expression: `localStorage.setItem('hrms_token', ${JSON.stringify(loginBody.data.token)}); localStorage.setItem('hrms_user', ${JSON.stringify(JSON.stringify(user))});`,
  });

  await capture(client, '/dashboard', '截图1_系统首页统计.png', '各岗位招聘漏斗');
  await capture(client, '/candidates', '截图2_候选人查询与CSV导出.png', '导出 CSV');
  await capture(client, '/workflow', '截图3_招聘流程测试页面.png', '简历筛选');
  await client.send('Runtime.evaluate', {
    expression: `localStorage.setItem('hrms_token', ${JSON.stringify(managerBody.data.token)}); localStorage.setItem('hrms_user', ${JSON.stringify(JSON.stringify(managerUser))});`,
  });
  await capture(client, '/positions', '截图4_岗位数据查询页面.png', '岗位管理');
  await client.ws.close();
} finally {
  chrome.kill();
  await sleep(500);
  await rm(profileDir, { recursive: true, force: true }).catch(() => {});
}
