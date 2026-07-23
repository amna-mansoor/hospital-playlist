# 🏥 Hospital Appointment & Bed Allocation System

A full-stack hospital management system built to demonstrate **concurrency-safe scheduling** with double-booking, race-condition, and resource-contention problems.

Patients book appointments against live doctor availability, admins manage departments/beds/doctors, and doctors run their day from a live dashboard, all while the system guarantees, at the database level, that two people can never be assigned the same appointment slot or the same hospital bed.

---

##  Features

- **Conflict-free appointment booking**: optimistic locking (`@Version`) plus a database-level `UNIQUE(doctor_id, slot_start)` constraint means a double-booking is not just unlikely, it's structurally impossible, even under concurrent load.
- **Bed & ward allocation**: pessimistic row-locking (`PESSIMISTIC_WRITE`) safely assigns the next free bed when two receptionists admit patients into the same department at the same instant. Live occupancy view (e.g. *"ICU: 8/10 occupied"*).
- **Waitlist queue**: if a doctor's day is fully booked, patients join a waitlist and get auto-emailed the moment a cancellation frees up a slot.
- **Role-based access**: JWT authentication with `PATIENT`, `DOCTOR`, `RECEPTIONIST`, and `ADMIN` roles, each with a purpose-built dashboard.
- **Doctor dashboard**: today's schedule on a FullCalendar view, patient history, mark visits as completed/no-show.
- **Admin analytics dashboard**: department load, doctor utilization %, peak booking hours, and live bed occupancy, all charted with Recharts.
- **Automated email notifications**: booking confirmations, 24-hour appointment reminders (`@Scheduled` cron job), and waitlist alerts, sent via Gmail SMTP.
- **Audit trail** — every cancellation, reschedule, and status change is logged with who did it and when.

---

##  Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3 (Web, Data JPA, Validation, Security) |
| Database | PostgreSQL (Neon / Supabase free tier) |
| Frontend | React 18 + Vite, FullCalendar, Recharts, Axios |
| Auth | Spring Security + JWT (role-based: Patient / Doctor / Receptionist / Admin) |
| Email | Gmail SMTP (app password) |

---

##  The concurrency problem this project solves

Two patients click "book" on the exact same doctor + time slot within milliseconds of each other. What happens?

1. Both requests read the slot as free.
2. Whichever request's `INSERT` reaches Postgres first wins and creates the appointment row.
3. The **`UNIQUE(doctor_id, slot_start)` constraint** on the `appointments` table means the second `INSERT` is physically rejected by the database itself — not just caught by application logic that could theoretically be raced around.
4. The losing request gets a clean `409 Conflict` — *"This slot was just booked. Please choose another time."* — instead of a silent double-booking or an ugly crash.

Beds use a deliberately different strategy: since beds are a scarcer, more contended resource, `BedRepository` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` so the second admission request simply **waits its turn** on the database row rather than failing and retrying. Two different flavors of the same underlying problem, solved with the right tool for each — see `AppointmentService.java` and `BedService.java` for the full comments.

---

##  Database design

Core tables: `users`, `patients`, `doctors`, `departments`, `appointments`, `beds`, `admissions`, `waitlist`, `audit_logs`.

- Foreign keys tie appointments/admissions back to patients, doctors, and departments.
- `UNIQUE(doctor_id, slot_start)` on `appointments` prevents double-booking at the schema level.
- Indexes on `(doctor_id, slot_start)` and `patient_id` keep slot lookups and history queries fast as data grows.
- `@Version` columns on `appointments` and `beds` back the optimistic-locking strategy described above.

---

## 📁 Project Structure

```text
hospital-system/
├── backend/                         # Spring Boot 3 REST API (Java 17)
│   ├── src/main/java/com/hospital/
│   │   ├── model/                   # JPA entities
│   │   ├── repository/              # Spring Data JPA repositories
│   │   ├── service/                 # Business logic (booking, beds, waitlist, email, audit)
│   │   ├── controller/              # REST endpoints
│   │   ├── security/                # JWT Filter + utilities
│   │   ├── config/                  # Spring Security configuration
│   │   ├── scheduled/               # @Scheduled reminder job
│   │   └── dto/                     # Request/response payloads
│   └── src/test/                    # JUnit smoke test + H2 test config
├── frontend/                        # React + Vite SPA
│   └── src/
│       ├── pages/                   # Login, Register, PatientBooking, DoctorDashboard, AdminDashboard
│       ├── components/
│       └── api/                     # Shared Axios client
└── .github/
    └── workflows/                   # GitHub Actions CI (runs backend tests on push)
```

---

##  Key API endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a Patient or Doctor account | Public |
| `POST` | `/api/auth/login` | Log in, returns a JWT | Public |
| `GET` | `/api/doctors` | List all doctors (name, department, hours) | Any logged-in user |
| `GET` | `/api/appointments/slots?doctorId=&date=` | Available slots for a doctor on a date | Any logged-in user |
| `POST` | `/api/appointments/book` | Book an appointment (concurrency-safe) | Patient |
| `POST` | `/api/appointments/{id}/cancel` | Cancel an appointment | Patient/Admin |
| `POST` | `/api/waitlist/join` | Join the waitlist for a full day | Patient |
| `POST` | `/api/beds/admit` | Admit a patient to the next free bed | Receptionist/Admin |
| `POST` | `/api/beds/discharge/{admissionId}` | Discharge and free the bed | Receptionist/Admin |
| `GET` | `/api/admin/analytics/*` | Department load, doctor utilization, peak hours, bed occupancy | Admin |

---

