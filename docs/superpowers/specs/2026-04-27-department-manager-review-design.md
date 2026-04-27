# Department Manager Review Design

## Goal

Add a department manager identity that manages positions and performs a second confirmation for candidates after HR passes resume screening.

## Roles

- `HR`: keeps the existing `admin / admin123` account. HR manages candidates, resume screening, interviews, offers, and statistics.
- `MANAGER`: adds a default `manager / manager123` account. Managers create and maintain positions, and confirm or reject HR-passed candidates.

Login responses include `role`, and JWT authentication loads that role into Spring Security authorities. The frontend stores the role in `hrms_user` and uses it for navigation and route guards.

## Candidate Flow

Resume screening owns both HR screening state and manager confirmation state:

- HR screening: `PENDING`, `PASSED`, `REJECTED`.
- Manager confirmation: `NOT_SUBMITTED`, `PENDING`, `APPROVED`, `REJECTED`.

When HR changes screening from a non-passed state to `PASSED`, manager confirmation becomes `PENDING`. If HR changes screening back to `PENDING` or `REJECTED`, manager confirmation resets to `NOT_SUBMITTED`. Managers can only confirm candidates whose HR screening status is `PASSED`.

Candidates can be scheduled for interview only when HR screening is `PASSED` and manager confirmation is `APPROVED`. Manager rejection blocks interview scheduling.

## Backend Surface

- Extend `Admin` with a `role` enum.
- Add manager confirmation fields to `ResumeScreening`.
- Add `/api/manager-reviews`:
  - `GET`: list HR-passed candidates for manager confirmation.
  - `PUT /{candidateId}`: approve or reject with an optional comment.
- Restrict position mutations to `MANAGER`.
- Restrict HR workflow mutations to `HR`.
- Keep shared read endpoints available to authenticated users where needed.

## Frontend Surface

- Show role-aware navigation.
- Add a manager confirmation view under `/manager-reviews`.
- Show manager confirmation status in candidate progress and workflow screening table.
- Keep the interview tab HR-facing; it will only receive candidates approved by a manager.

## Testing

Backend tests cover role-aware login, manager-only position creation, HR screening handoff to manager confirmation, manager approval enabling interview scheduling, and manager rejection blocking interviews. Frontend verification uses `npm run build`.
