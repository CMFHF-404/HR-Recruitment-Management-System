import os
import sqlite3
import uuid
from datetime import datetime

import pandas as pd
import streamlit as st


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
UPLOAD_DIR = os.path.join(BASE_DIR, "uploads")
DB_PATH = os.path.join(DATA_DIR, "hr_recruitment_lab3.db")

os.makedirs(DATA_DIR, exist_ok=True)
os.makedirs(UPLOAD_DIR, exist_ok=True)


def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def init_tables():
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        CREATE TABLE IF NOT EXISTS position (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            department TEXT NOT NULL,
            status TEXT NOT NULL CHECK(status IN ('OPEN','CLOSED'))
        )
    """)
    cur.execute("""
        CREATE TABLE IF NOT EXISTS candidate_entry (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            gender TEXT NOT NULL CHECK(gender IN ('男','女')),
            phone TEXT NOT NULL UNIQUE,
            email TEXT NOT NULL UNIQUE,
            education TEXT NOT NULL,
            school TEXT NOT NULL,
            position_id INTEGER NOT NULL,
            resume_path TEXT,
            note TEXT,
            ai_label TEXT NOT NULL,
            ai_confidence REAL NOT NULL CHECK(ai_confidence >= 0 AND ai_confidence <= 1),
            status TEXT NOT NULL CHECK(status IN ('草稿','已提交','已审核')),
            created_at TEXT NOT NULL,
            FOREIGN KEY(position_id) REFERENCES position(id)
        )
    """)
    count = cur.execute("SELECT COUNT(*) FROM position").fetchone()[0]
    if count == 0:
        cur.executemany(
            "INSERT INTO position(name, department, status) VALUES(?,?,?)",
            [
                ("Java 开发工程师", "技术部", "OPEN"),
                ("前端开发工程师", "技术部", "OPEN"),
                ("人事专员", "人力资源部", "OPEN"),
            ],
        )
    conn.commit()
    conn.close()


def save_uploaded_file(uploaded_file):
    if uploaded_file is None:
        return ""
    allowed = [".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png", ".txt"]
    ext = os.path.splitext(uploaded_file.name)[1].lower()
    if ext not in allowed:
        raise ValueError("附件只允许上传 pdf、doc、docx、jpg、jpeg、png、txt 格式。")
    if uploaded_file.size > 5 * 1024 * 1024:
        raise ValueError("上传文件不能超过 5MB。")
    name = f"{uuid.uuid4().hex}{ext}"
    path = os.path.join(UPLOAD_DIR, name)
    with open(path, "wb") as f:
        f.write(uploaded_file.getbuffer())
    return os.path.relpath(path, BASE_DIR)


def ai_judge(education, note, has_attachment):
    text = f"{education} {note}".lower()
    if any(word in text for word in ["spring", "java", "vue", "项目", "经验", "硕士"]):
        return "高匹配", 0.88
    if not has_attachment:
        return "待补充材料", 0.56
    return "一般匹配", 0.72


def validate_form(form):
    errors = []
    if not form["name"].strip():
        errors.append("姓名不能为空。")
    if not form["phone"].strip():
        errors.append("联系电话不能为空。")
    if "@" not in form["email"]:
        errors.append("邮箱格式不正确。")
    if not form["school"].strip():
        errors.append("毕业院校不能为空。")
    if not form["education"].strip():
        errors.append("学历不能为空。")
    if not form["position_id"]:
        errors.append("请选择应聘岗位。")
    return errors


def insert_candidate(record):
    conn = get_connection()
    cur = conn.cursor()
    cur.execute("""
        INSERT INTO candidate_entry (
            name, gender, phone, email, education, school, position_id,
            resume_path, note, ai_label, ai_confidence, status, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        record["name"], record["gender"], record["phone"], record["email"],
        record["education"], record["school"], record["position_id"],
        record["resume_path"], record["note"], record["ai_label"],
        record["ai_confidence"], record["status"], record["created_at"],
    ))
    conn.commit()
    conn.close()


def load_positions():
    conn = get_connection()
    rows = pd.read_sql_query("SELECT id, name, department FROM position WHERE status='OPEN' ORDER BY id", conn)
    conn.close()
    return rows


def load_candidates():
    conn = get_connection()
    rows = pd.read_sql_query("""
        SELECT c.id, c.name, c.phone, c.email, c.education, p.name AS position_name,
               c.resume_path, c.ai_label, c.ai_confidence, c.status, c.created_at
        FROM candidate_entry c
        JOIN position p ON p.id = c.position_id
        ORDER BY c.id DESC
    """, conn)
    conn.close()
    return rows


init_tables()
st.set_page_config(page_title="小型企业招聘管理系统", layout="wide")
st.title("小型企业招聘管理系统（第3回原型）")
st.caption("界面原型 + 数据录入、数据校验、附件上传与 SQLite 写入")

menu = st.sidebar.radio("功能菜单", ["数据录入", "数据预览", "实验说明"])

if menu == "数据录入":
    st.subheader("候选人资料录入")
    positions = load_positions()
    position_options = {f"{row.name} / {row.department}": int(row.id) for row in positions.itertuples()}
    with st.form("candidate_form"):
        col1, col2 = st.columns(2)
        name = col1.text_input("姓名 *")
        gender = col2.selectbox("性别 *", ["男", "女"])
        phone = col1.text_input("联系电话 *")
        email = col2.text_input("邮箱 *")
        education = col1.text_input("学历 *", value="本科")
        school = col2.text_input("毕业院校 *")
        position_label = st.selectbox("应聘岗位 *", list(position_options.keys()))
        status = st.selectbox("记录状态 *", ["草稿", "已提交", "已审核"], index=1)
        resume = st.file_uploader("简历或附件", type=["pdf", "doc", "docx", "jpg", "jpeg", "png", "txt"])
        note = st.text_area("备注/经历摘要", value="熟悉 Spring Boot 和 Vue，有课程项目经验。")
        submitted = st.form_submit_button("保存记录")
    if submitted:
        form = {
            "name": name,
            "gender": gender,
            "phone": phone,
            "email": email,
            "education": education,
            "school": school,
            "position_id": position_options.get(position_label),
        }
        errors = validate_form(form)
        if errors:
            for error in errors:
                st.error(error)
        else:
            try:
                resume_path = save_uploaded_file(resume)
                ai_label, ai_confidence = ai_judge(education, note, bool(resume_path))
                insert_candidate({
                    **form,
                    "resume_path": resume_path,
                    "note": note,
                    "ai_label": ai_label,
                    "ai_confidence": ai_confidence,
                    "status": status,
                    "created_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                })
                st.success("记录保存成功！")
                st.info(f"AI 辅助判断：{ai_label}（置信度 {ai_confidence:.2f}）")
                if resume_path:
                    st.write(f"附件保存路径：{resume_path}")
            except (sqlite3.IntegrityError, ValueError) as exc:
                st.error(f"保存失败：{exc}")

elif menu == "数据预览":
    st.subheader("候选人数据预览")
    data = load_candidates()
    st.dataframe(data, use_container_width=True)

else:
    st.subheader("实验说明")
    st.markdown("""
    - 主表：candidate_entry
    - 已实现校验：必填、邮箱格式、附件格式与大小、数据库 UNIQUE/CHECK/FOREIGN KEY
    - 附件保存目录：uploads/
    - 数据库文件：data/hr_recruitment_lab3.db
    """)
