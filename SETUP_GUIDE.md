# Hospital Appointment & Bed Allocation System — Complete Setup Guide

This guide assumes you have **never set up a coding project before**. Every
step is spelled out. Do them in order. It will take a few hours the first
time; skip nothing.

## What you're building, in plain English

- A **backend** (the "brain"): a Java program that stores data and enforces
  rules — like "two patients can never book the exact same appointment slot."
- A **database**: a filing cabinet in the cloud (Postgres) where patients,
  doctors, appointments, and beds are stored permanently.
- A **frontend** (the "face"): a website built in React where patients,
  doctors, and admins actually click buttons and see things.
- **Email**: your Gmail account sends confirmation and reminder emails.

All four pieces run separately and talk to each other over the internet.
Locally, you'll run the backend and frontend on your own computer to build
and test. Then you'll put each piece on a free cloud host so it's live
24/7 without you needing your computer on.

---

## Part 1 — Create your free accounts

Do all of these now; you'll need them throughout. All are free tiers.

1. **GitHub** (stores your code): go to https://github.com/join, sign up
   with an email, verify your email address.
2. **Neon** (free Postgres database): go to https://neon.tech, click
   "Sign up", and sign up with your GitHub account (easiest — just click
   "Continue with GitHub"). *(Supabase.com works identically if you prefer
   it — the steps below are for Neon, but Supabase's "connection string"
   screen looks almost the same.)*
3. **Render** (hosts your backend): go to https://render.com, sign up with
   "Continue with GitHub."
4. **Vercel** (hosts your frontend): go to https://vercel.com, sign up with
   "Continue with GitHub."
5. **Gmail**: use an existing Gmail account, or create a free one at
   https://accounts.google.com/signup. You'll set up an "app password" for
   it in Part 8 — don't do that yet.

---

## Part 2 — Install tools on your computer

You need four things installed. Install them in this order.

### 2.1 Git (lets your computer talk to GitHub)
- Windows: download from https://git-scm.com/download/win, run the
  installer, click "Next" through all the default options.
- Mac: open the "Terminal" app (search for it with Spotlight, the
  magnifying glass icon top-right), type `git --version` and press Enter —
  macOS will offer to install it for you automatically. Click "Install."
- Verify: open Terminal (Mac) or "Git Bash" (Windows, installed with Git),
  type `git --version`. You should see a version number.

### 2.2 Java 17 (runs the backend)
- Go to https://adoptium.net, it auto-detects your operating system.
  Download the "LTS" version marked **17**.
- Run the installer, accept defaults.
- Verify: in Terminal/Git Bash, type `java -version`. You should see
  something starting with `17.`.

### 2.3 Apache Maven (builds the Java project)
- Go to https://maven.apache.org/download.cgi, download the "Binary zip
  archive."
- Unzip it somewhere permanent, e.g. `C:\maven` (Windows) or
  `/usr/local/maven` (Mac).
- Add it to your PATH so your computer can find the `mvn` command:
  - **Windows**: search "Environment Variables" in the Start menu → "Edit
    the system environment variables" → "Environment Variables" button →
    under "System variables" find "Path" → "Edit" → "New" → paste the path
    to the `bin` folder inside where you unzipped Maven (e.g.
    `C:\maven\bin`) → OK on everything.
  - **Mac**: in Terminal, run
    `echo 'export PATH="/usr/local/maven/bin:$PATH"' >> ~/.zshrc` then
    `source ~/.zshrc` (adjust the path to wherever you unzipped it).
- Verify: open a **new** Terminal/Git Bash window (important — old windows
  won't see the update), type `mvn -version`. You should see a version
  number and "Java version: 17...".

### 2.4 Node.js (runs the frontend)
- Go to https://nodejs.org, download the "LTS" version, run the installer
  with default options.
- Verify: in Terminal/Git Bash, type `node -v` and `npm -v`. Both should
  print version numbers.

### 2.5 A code editor
- Download VS Code (free): https://code.visualstudio.com. Install with
  defaults. This is where you'll open and read the project's files.

---

## Part 3 — Get the project onto your computer and into GitHub

1. On GitHub.com, click the "+" icon top-right → "New repository."
   Name it `hospital-system`, leave it "Public" (or Private if you prefer),
   do **not** check any of the "initialize with" boxes. Click "Create
   repository." Keep this page open — GitHub will show you commands.
2. On your computer, open Terminal/Git Bash and navigate to wherever you'd
   like the project folder to live, e.g.:
   ```
   cd Desktop
   ```
3. Copy the entire `hospital-system` project folder (the one you were
   given alongside this guide) into that location, so you end up with e.g.
   `Desktop/hospital-system` containing the `backend/`, `frontend/`, and
   `.github/` folders plus this guide.
4. Turn it into a Git project and push it to GitHub:
   ```
   cd hospital-system
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/hospital-system.git
   git push -u origin main
   ```
   Replace `YOUR-USERNAME` with your actual GitHub username (GitHub shows
   you this exact URL on the "new repository" page from step 1). The first
   push may open a browser window asking you to log in to GitHub — do so.
5. Refresh the GitHub page — you should now see all your files there.

---

## Part 4 — Set up your free Postgres database (Neon)

1. Log into https://neon.tech, click "Create a project."
2. Name it `hospital-system`, pick the region closest to you, click
   "Create project."
3. Neon shows you a **connection string** that looks like:
   ```
   postgresql://alex:AbCd1234@ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```
   Click "Copy" and paste it somewhere safe temporarily (a Notes app) —
   you'll need pieces of it in the next step. This string secretly
   contains your username, password, host address, and database name all
   together.
4. Break that string into its four parts (this matters for Part 5):
   - **Username**: the part right after `postgresql://` and before the `:`
     (in the example above: `alex`)
   - **Password**: the part between the `:` and the `@` (in the example:
     `AbCd1234`)
   - **Host**: the part between `@` and the next `/` (in the example:
     `ep-cool-name-123456.us-east-2.aws.neon.tech`)
   - **Database name**: the part after the last `/` and before the `?`
     (in the example: `neondb`)

You don't need to create any tables manually — the backend will create
them automatically the first time it runs (see the `ddl-auto=update`
setting in `application.properties`, already configured for you).

---

## Part 5 — Configure and run the backend on your computer

Spring Boot reads secrets from **environment variables** — a fancy term
for "values you set in your terminal session before running the program",
rather than hard-coding them into files that end up on GitHub for the
world to see.

1. Open Terminal/Git Bash, navigate into the backend folder:
   ```
   cd hospital-system/backend
   ```
2. Set the required environment variables. Using the pieces from Part 4,
   and picking any long random string for `JWT_SECRET` (this signs your
   login tokens — treat it like a password; 40+ random characters is
   great):

   **Mac/Linux (Terminal):**
   ```
   export DB_URL="jdbc:postgresql://ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require"
   export DB_USERNAME="alex"
   export DB_PASSWORD="AbCd1234"
   export JWT_SECRET="paste-a-long-random-string-here-at-least-40-characters"
   export GMAIL_USERNAME="youraddress@gmail.com"
   export GMAIL_APP_PASSWORD="will-fill-this-in-part-8"
   ```

   **Windows (Git Bash):** same commands as above work in Git Bash. If
   you're using plain Windows Command Prompt instead, use `set` instead of
   `export` and no quotes, e.g. `set DB_URL=jdbc:postgresql://...`

   Note the `DB_URL` starts with `jdbc:postgresql://` — that's different
   from the `postgresql://` Neon gave you; Spring Boot's database driver
   needs the `jdbc:` prefix added on the front.

   Also note: these `export` commands only last for your current terminal
   window. You'll need to re-run them (or use a tool like `direnv`, or
   just paste them again) every time you open a new terminal to run the
   backend locally.

3. Leave `GMAIL_APP_PASSWORD` blank/placeholder for now — the app will
   still start, it just can't send real emails yet until Part 8.
4. Build and run:
   ```
   mvn spring-boot:run
   ```
5. Watch the output. The first run downloads a lot of libraries (takes a
   few minutes) then you should see a line like:
   ```
   Tomcat started on port(s): 8080
   ```
   That means your backend is alive at `http://localhost:8080`. Leave this
   terminal window open and running — closing it stops the backend.
6. Quick sanity check: open a **second** terminal window and run:
   ```
   curl http://localhost:8080/actuator/health
   ```
   You should see `{"status":"UP"}`.

**If it fails to start**, scroll up in the terminal output — the real
error is usually near the top of the stack trace, not the bottom. Common
causes: a typo in `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, or you're using an
old terminal window that doesn't have the `export` commands from this
session (re-run them).

---

## Part 6 — Seed your first data (departments, doctors, beds)

The system starts completely empty — no departments, doctors, or beds
exist yet. You (as the future "admin") need to create the very first
admin account directly in the database once, then use it to create
everything else through the API.

### 6.1 Create the first admin account

Since the registration page only allows Patient/Doctor self-signup (by
design — you don't want strangers making themselves Admin!), create the
first admin manually using Neon's built-in SQL editor:

1. In Neon, open your project → click "SQL Editor" in the left sidebar.
2. First register a normal account through the running frontend (Part 7)
   or via curl, e.g.:
   ```
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"fullName":"Head Admin","email":"admin@example.com","password":"ChangeMe123","role":"PATIENT"}'
   ```
3. In Neon's SQL Editor, promote that account to ADMIN:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
   ```
4. Log in again through the frontend (or via `/api/auth/login`) with that
   same email/password — you'll now get back `"role":"ADMIN"` and see the
   Admin Dashboard.

### 6.2 Create departments, doctors, and beds

Log in as the admin, copy the `token` you get back, and use it to call
the admin setup endpoints (the easiest way is directly through the
frontend once it's running — Part 7 — using your browser's address bar
plus a tool like Postman/Insomnia (free) if you want a nicer interface
than `curl`). Example with `curl`:

```
TOKEN="paste-your-jwt-token-here"

# 1. Create a department
curl -X POST "http://localhost:8080/api/admin/departments?name=Cardiology&description=Heart%20care" \
  -H "Authorization: Bearer $TOKEN"

# 2. Register a doctor account first (as PATIENT/DOCTOR self-signup, role=DOCTOR)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Dr. Amina Khan","email":"amina@example.com","password":"DoctorPass1","role":"DOCTOR"}'

# 3. Look up that doctor's user id in Neon's SQL editor:
#    SELECT id, email FROM users WHERE email = 'amina@example.com';
# Suppose it returns id = 2

# 4. Turn that user into a bookable Doctor profile (Monday=1 .. Sunday=7)
curl -X POST "http://localhost:8080/api/admin/doctors?userId=2&departmentId=1&specialization=Cardiologist&dayOfWeek=1&shiftStart=09:00&shiftEnd=13:00&slotLengthMinutes=30" \
  -H "Authorization: Bearer $TOKEN"

# 5. Add some beds to the department
curl -X POST "http://localhost:8080/api/admin/beds?departmentId=1&bedCode=CARD-01" \
  -H "Authorization: Bearer $TOKEN"
curl -X POST "http://localhost:8080/api/admin/beds?departmentId=1&bedCode=CARD-02" \
  -H "Authorization: Bearer $TOKEN"
```

Repeat step 4-5 for every doctor/bed you want. This manual "admin setup"
step is a one-time chore per doctor/department — a natural next
improvement (see "Where to go from here" at the end) would be building an
actual Admin UI screen for this instead of using curl.

---

## Part 7 — Configure and run the frontend on your computer

1. Open a **new** terminal window (leave the backend running in the old
   one). Navigate to the frontend folder:
   ```
   cd hospital-system/frontend
   ```
2. Install all the frontend's dependencies (one-time, downloads React and
   friends):
   ```
   npm install
   ```
3. Create your local environment file:
   ```
   cp .env.example .env
   ```
   Open `.env` in VS Code — it should already point at
   `http://localhost:8080`, which is correct for local development.
4. Start the frontend:
   ```
   npm run dev
   ```
5. It will print something like `Local: http://localhost:5173/`. Open that
   address in your browser (Chrome/Firefox/Edge, any modern browser).
6. You should see the Hospital System navigation bar. Click "Register",
   create a Patient account, log in, and try booking an appointment
   against a doctor you seeded in Part 6.

**To prove the concurrency-safety works:** open the booking page in two
different browser tabs (or one normal + one incognito window, logged in
as two different patients), pick the exact same doctor and slot in both,
and click "book" in both tabs within a second of each other. One will
succeed; the other will show the friendly "this slot was just booked by
another patient" message instead of creating a duplicate booking. That's
the optimistic-locking + unique-constraint safety net described in
`AppointmentService.java` doing its job.

---

## Part 8 — Set up Gmail so the app can send real emails

Gmail won't let apps log in with your normal password anymore — you need
a special 16-character "app password" instead.

1. Turn on 2-Step Verification if you haven't already: go to
   https://myaccount.google.com/security, under "How you sign in to
   Google" click "2-Step Verification," follow the prompts (usually
   verifying via a code texted to your phone).
2. Once that's on, go to https://myaccount.google.com/apppasswords
   (you may need to sign in again).
3. Under "App name", type something like `hospital-system` and click
   "Create."
4. Google shows you a 16-character password like `abcd efgh ijkl mnop`.
   Copy it (remove the spaces isn't necessary — Gmail accepts it with or
   without spaces, but it's simplest to keep it as one string without
   spaces when you paste it into an environment variable).
5. Back in your backend terminal, set/update:
   ```
   export GMAIL_USERNAME="youraddress@gmail.com"
   export GMAIL_APP_PASSWORD="abcdefghijklmnop"
   ```
6. Restart the backend (`Ctrl+C` to stop it, then `mvn spring-boot:run`
   again) so it picks up the new values.
7. Book a test appointment through the frontend — you should receive a
   real confirmation email within a few seconds.

---

## Part 9 — Deploy the backend live (Render.com, free, no Docker)

1. Make sure all your latest code is pushed to GitHub (`git add .`,
   `git commit -m "..."`, `git push`).
2. Log into https://render.com. Click "New +" → "Web Service."
3. Connect your GitHub account if prompted, then pick your
   `hospital-system` repository.
4. Render needs to know your backend lives in the `backend` subfolder.
   Fill in:
   - **Name**: `hospital-backend` (or anything you like)
   - **Root Directory**: `backend`
   - **Runtime**: `Java`
   - **Build Command**: `./mvnw clean install -DskipTests` (Maven Wrapper
     — see note below) — or simply `mvn clean install -DskipTests` if you
     don't have a wrapper committed.
   - **Start Command**: `java -jar target/hospital-system-1.0.0.jar`
   - **Instance Type**: Free
5. Scroll to "Environment Variables" and add each of these (same values
   you used locally in Part 5 and Part 8):
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `GMAIL_USERNAME`
   - `GMAIL_APP_PASSWORD`
6. Click "Create Web Service." Render will build and deploy — the first
   build takes several minutes. Watch the logs; when you see "Tomcat
   started on port(s)", it's live.
7. Render gives your service a public URL like
   `https://hospital-backend.onrender.com`. Test it:
   ```
   curl https://hospital-backend.onrender.com/actuator/health
   ```
   Should return `{"status":"UP"}`.

**Note on the Maven Wrapper**: if the build command `./mvnw ...` fails
because there's no `mvnw` file in your project, simply use
`mvn clean install -DskipTests` instead as the Build Command — Render's
Java environment already has Maven installed, so this works fine without
Docker or a wrapper.

**Free tier heads-up**: Render's free web services "spin down" after 15
minutes of no traffic and take ~30-60 seconds to "wake up" on the next
request. This is normal and fine for a portfolio project — just mention
it if you're demoing live to someone.

---

## Part 10 — Deploy the frontend live (Vercel, free)

1. Push your latest code to GitHub if you haven't already.
2. Log into https://vercel.com. Click "Add New..." → "Project."
3. Import your `hospital-system` GitHub repository.
4. Vercel needs to know the frontend lives in a subfolder:
   - **Root Directory**: click "Edit" and select `frontend`.
   - Framework Preset: Vercel usually auto-detects "Vite" — leave it.
5. Under "Environment Variables", add:
   - `VITE_API_BASE_URL` = your Render backend URL from Part 9, e.g.
     `https://hospital-backend.onrender.com` (no trailing slash).
6. Click "Deploy." After a minute or two you'll get a live URL like
   `https://hospital-system-yourname.vercel.app`.
7. Open it, register an account, and confirm it can talk to your live
   backend (try logging in — if you see a network error, double check the
   `VITE_API_BASE_URL` value and that your backend's CORS settings in
   `SecurityConfig.java` include your actual Vercel domain, which the
   provided config already allows via the `*.vercel.app` wildcard).

---

## Part 11 — Confirm GitHub Actions CI is running your tests

This was already set up for you as a file at
`.github/workflows/backend-ci.yml`. It's just a set of instructions that
tells GitHub: "every time code is pushed, spin up a fresh computer, install
Java, and run `mvn test`."

1. Go to your repository on GitHub.com.
2. Click the "Actions" tab near the top.
3. You should see a run called "Backend CI" corresponding to your last
   push, with a green checkmark (passed) or red X (failed).
4. Click into it to see the log output if you're curious or something
   failed. This is entirely free for public repos and gives you a genuine
   "tests pass on every commit" badge you can point to.

---

## Part 12 — How the "no double-booking" concurrency safety actually works

You asked for this, and it's genuinely one of the most interesting parts
of the system, so here's the plain-English recap (the full explanation
also lives as comments directly in `Appointment.java` and
`AppointmentService.java`):

- Every `appointments` row has a hidden `version` number.
- When two patients try to grab the same slot within milliseconds of each
  other, both read the slot as "free" at first.
- Whoever's booking request reaches the database first wins and creates
  the row.
- The database itself has a **unique constraint** on (doctor, slot time) —
  so even if both requests somehow tried to insert at the exact same
  instant, the database physically refuses to allow two rows for the same
  doctor+slot to exist. It's not just an application-code check; it's
  enforced at the lowest level, the same way a spreadsheet can refuse
  duplicate entries in a column marked "no duplicates."
- The loser's request gets a clean "sorry, that slot was just taken"
  message instead of crashing or silently double-booking.
- Beds use a slightly different, stricter technique (`PESSIMISTIC_WRITE`
  locking) because beds are a scarcer resource — instead of "fail and
  retry", the second request for a bed simply *waits* politely until the
  first one finishes, then gets the next free bed.

This distinction (optimistic locking for appointments vs. pessimistic
locking for beds) is worth mentioning explicitly if you ever discuss this
project in an interview — it shows you picked the right tool for two
different flavors of the same underlying problem.

---

## Troubleshooting quick-reference

| Problem | Likely fix |
|---|---|
| `mvn` command not found | Re-check Part 2.3 — the PATH wasn't set correctly, or you're using an old terminal window |
| Backend won't connect to database | Double-check `DB_URL` starts with `jdbc:postgresql://` and the username/password have no typos/extra quotes |
| Emails never arrive | Confirm 2-Step Verification is on and you used an **App Password**, not your normal Gmail password |
| Frontend shows network errors | Check `VITE_API_BASE_URL` in `.env` (local) or Vercel's environment variables (deployed) points at the right backend URL, with no trailing slash |
| Render deploy fails on build | Try changing Build Command to `mvn clean install -DskipTests` instead of `./mvnw ...` |
| CORS error in browser console | Add your actual frontend domain to the list in `SecurityConfig.java`'s `corsConfigurationSource()` and redeploy the backend |
| Booking two tabs both succeed | Make sure you're testing against a doctor/slot combination where both tabs are hitting the *same* running backend instance — not two different local runs |

---

## Where to go from here (nice, optional next steps)

- Build a real Admin UI for the "seed data" steps in Part 6 instead of
  using `curl` (a simple set of React forms hitting the same endpoints).
- Add doctor "block out leave/breaks" as its own admin screen instead of
  the single `onLeave` boolean.
- Swap `spring.jpa.hibernate.ddl-auto=update` for a real migration tool
  (Flyway) once the schema stabilizes — better practice for real
  production systems.
- Add pagination to the doctor/patient history views once you have more
  than a handful of test records.

You now have a complete, working, deployed, concurrency-safe hospital
scheduling system — end to end, for free. Nice work.
