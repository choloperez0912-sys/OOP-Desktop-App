# CalcApp — Java Desktop Calculator

A desktop calculator app built with **Java Swing** + **Supabase** backend.

**Features:**
- Login & Register (passwords hashed with SHA-256)
- Full calculator with expression preview
- Calculation history saved to Supabase
- History view with refresh & clear
- Keyboard support
- Secure environment variables via `.env` file

---

## Prerequisites

| Tool | Version | Download |
|------|---------|---------|
| JDK  | 11 or higher | https://adoptium.net |
| VS Code | Any | https://code.visualstudio.com |
| Extension Pack for Java | VS Code extension | Search in Extensions panel |

---

## Step 1 — Set up Supabase database

1. Go to [supabase.com](https://supabase.com) and open your project
2. Click **SQL Editor → New Query**
3. Copy and paste the contents of `supabase_setup.sql`
4. Click **Run**

This creates the `users` and `calc_history` tables with the correct policies.

---

## Step 2 — Configure environment

The `.env` file holds your credentials and is **never committed to Git**.

It already exists in this project with your keys pre-filled.  
To use a different Supabase project, edit `.env`:

```
APP_ENV=development
SUPABASE_URL=https://YOUR_PROJECT.supabase.co/rest/v1
SUPABASE_KEY=YOUR_ANON_KEY
```

---

## Step 3 — Open in VS Code

```bash
# Clone or copy the project folder, then:
code CalcApp
```

VS Code will detect the Java project automatically via `.vscode/settings.json`.

---

## Step 4 — Run the app

**Option A — VS Code (recommended)**
1. Open `src/Main.java`
2. Click the **▶ Run** button above `main()`, OR
3. Press `F5` (uses `.vscode/launch.json`)

**Option B — Terminal**
```bash
# Compile
/path/to/jdk/bin/javac -d out $(find src -name "*.java")

# Run (from project root so .env is found)
/path/to/jdk/bin/java -cp out Main
```

> ⚠️ Always run from the **project root folder** so the `.env` file is found.

---

## Project Structure

```
CalcApp/
├── src/
│   ├── Main.java                         ← Entry point
│   └── com/calcapp/
│       ├── config/
│       │   ├── AppConfig.java            ← Loads .env / env vars
│       │   └── Json.java                 ← Zero-dependency JSON util
│       ├── model/
│       │   ├── User.java
│       │   └── CalcHistory.java
│       ├── service/
│       │   ├── SupabaseService.java      ← All HTTP calls to Supabase
│       │   ├── SessionManager.java       ← Logged-in user state
│       │   └── CalculatorEngine.java     ← Math expression evaluator
│       └── ui/
│           ├── Theme.java                ← Colors & fonts
│           ├── StyledButton.java         ← Custom rounded button
│           ├── MainWindow.java           ← JFrame + CardLayout router
│           ├── LoginPanel.java           ← Login & Register tabs
│           ├── CalculatorPanel.java      ← Calculator view
│           └── HistoryPanel.java         ← History view
├── .env                                  ← 🔒 Secrets (git-ignored)
├── .env.example                          ← Safe template to commit
├── .gitignore
├── supabase_setup.sql                    ← Run once in Supabase
└── .vscode/
    ├── launch.json                       ← F5 run config
    └── settings.json                     ← Java source paths
```

---

## Uploading to GitHub

The `.gitignore` already excludes `.env` and compiled output.

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/CalcApp.git
git push -u origin main
```

> Your API keys in `.env` are **never uploaded**. Only `.env.example` goes to GitHub.  
> Anyone cloning the repo copies `.env.example` → `.env` and fills in their own keys.

---

## Calculator Usage

| Input | Action |
|-------|--------|
| Click buttons | Enter digits/operators |
| Keyboard | Type numbers and `+ - * / % ( )` |
| Enter / `=` | Evaluate |
| Backspace | Delete last character |
| `C` key | Clear |

---

## Security Notes

- Passwords are hashed with **SHA-256** before storing
- API key is read from `.env`, never hardcoded (fallback default for dev only)
- `.env` is in `.gitignore` — safe to push to GitHub
