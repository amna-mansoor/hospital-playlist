# Hospital Appointment & Bed Allocation System

A full-stack hospital management system with conflict-free appointment
scheduling (via JPA optimistic locking + a database unique constraint),
bed/ward allocation with real-time occupancy, a waitlist queue, email
notifications, and role-based dashboards for patients, doctors, and admins.

**Don't read code first — read `SETUP_GUIDE.md`.** It walks through
absolutely every step, written for someone who has never set up a
developer project before: installing tools, creating free cloud accounts,
configuring the database and email, running everything locally, and
deploying it live on the internet for free.

## Folders

- `backend/` — Java 17 + Spring Boot REST API
- `frontend/` — React (Vite) single-page app
- `.github/workflows/` — GitHub Actions CI (runs backend tests on every push)
- `SETUP_GUIDE.md` — the full A-Z walkthrough
