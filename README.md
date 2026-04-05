# Exchange Rate Desktop Application — Sprint 3

**EECE 430L | Amr El Masri | Student ID: 202400971**

A JavaFX desktop client for the Exchange Rate Software System. This application consumes the Sprint 1 Flask backend REST API and provides a full desktop interface for exchange rate viewing, analytics, charting, transaction management, CSV export, P2P marketplace, and exchange rate alerts.

---

## Platform Requirements

| Requirement | Details |
|---|---|
| **Java JDK** | JDK 17 or higher (JDK 22 recommended — tested on JDK 22.0.2) |
| **JavaFX SDK** | JavaFX 22.0.1 (managed via Maven — no separate download needed) |
| **Build Tool** | Apache Maven 3.8+ |
| **IDE** | IntelliJ IDEA 2024+ (recommended) |
| **OS** | Windows 10/11, macOS, or Linux |
| **Backend** | Sprint 1 Flask backend must be running locally on port 5000 |

---

## Project Structure

```
exchange/
├── src/
│   ├── main/
│   │   ├── java/com/amr/exchange/
│   │   │   ├── Main.java                  # Application entry point
│   │   │   ├── Parent.java                # Navigation shell controller
│   │   │   ├── Authentication.java        # JWT token manager (Preferences API)
│   │   │   ├── PageCompleter.java         # Interface for completable screens
│   │   │   ├── OnPageCompleteListener.java
│   │   │   ├── api/
│   │   │   │   ├── Exchange.java          # Retrofit interface (all endpoints)
│   │   │   │   ├── ExchangeService.java   # Retrofit builder (base URL config)
│   │   │   │   └── model/                 # Data model classes
│   │   │   │       ├── User.java
│   │   │   │       ├── Token.java
│   │   │   │       ├── ExchangeRates.java
│   │   │   │       ├── Analytics.java
│   │   │   │       ├── RateHistory.java
│   │   │   │       ├── RateHistoryPoint.java
│   │   │   │       ├── Transaction.java
│   │   │   │       ├── Offer.java
│   │   │   │       └── Alert.java
│   │   │   ├── login/Login.java
│   │   │   ├── register/Register.java
│   │   │   ├── dashboard/Dashboard.java
│   │   │   ├── graph/Graph.java
│   │   │   ├── transactions/Transactions.java
│   │   │   ├── export/Export.java
│   │   │   ├── marketplace/Marketplace.java
│   │   │   └── alerts/Alerts.java
│   │   └── resources/com/amr/exchange/
│   │       ├── parent.fxml / parent.css
│   │       ├── login/login.fxml / login.css
│   │       ├── register/register.fxml / register.css
│   │       ├── dashboard/dashboard.fxml / dashboard.css
│   │       ├── graph/graph.fxml / graph.css
│   │       ├── transactions/transactions.fxml / transactions.css
│   │       ├── export/export.fxml / export.css
│   │       ├── marketplace/marketplace.fxml / marketplace.css
│   │       └── alerts/alerts.fxml / alerts.css
│   └── module-info.java
└── pom.xml
```

---

## Backend API Base URL Configuration

The backend base URL is defined in **one single place**:

```
src/main/java/com/amr/exchange/api/ExchangeService.java
```

```java
public class ExchangeService {
    static String API_URL = "http://localhost:5000";  // ← change this line
    ...
}
```

To point the app at a different backend (e.g. a remote server or a different port), edit `API_URL` in `ExchangeService.java` and rebuild.

---

## Setup Instructions

### Step 1 — Clone the Repository

```bash
git clone <your-repo-url>
cd exchange
```

### Step 2 — Verify Maven Dependencies

The project uses Maven to manage dependencies. The required libraries are declared in `pom.xml`:

- `org.openjfx:javafx-controls:22.0.1`
- `org.openjfx:javafx-fxml:22.0.1`
- `com.squareup.retrofit2:retrofit:2.9.0`
- `com.squareup.retrofit2:converter-gson:2.9.0`

Maven will download these automatically on first build. No manual downloads are required.

### Step 3 — Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Select **File → Open** and choose the `exchange/` folder (the one containing `pom.xml`)
3. Wait for IntelliJ to import the Maven project and index dependencies
4. Verify there are no red underlines in `module-info.java`

### Step 4 — Start the Sprint 1 Backend

The desktop app requires the Flask backend to be running. In a separate terminal:

```bash
cd <your-sprint1-backend-folder>
source venv/bin/activate        # Mac/Linux
venv\Scripts\activate           # Windows
python app.py
```

Confirm the backend is running at `http://127.0.0.1:5000`.

---

## Running the Application

### Option A — Run from IntelliJ IDEA (Recommended)

1. In the Project panel, locate `src/main/java/com/amr/exchange/Main.java`
2. Right-click `Main.java` → **Run 'Main.main()'**
3. The desktop window will open

### Option B — Run via Maven

```bash
cd exchange
mvn clean javafx:run
```

> **Note:** If using Maven CLI, ensure `JAVA_HOME` points to JDK 17+.

---

## First-Time Setup: Creating an Admin Account

All users register as `USER` by default. To promote a user to `ADMIN`, run this SQL in MySQL Workbench or your MySQL shell:

```sql
UPDATE user SET role = 'ADMIN' WHERE user_name = 'your_username';
```

---

## Authentication & Token Storage

- JWT tokens are stored using the **Java Preferences API** (`java.util.prefs.Preferences`)
- On **Windows**: stored in the registry under `HKEY_CURRENT_USER\Software\JavaSoft\Prefs`
- On **Mac/Linux**: stored in `~/.java/.userPrefs/`
- The token key is `TOKEN`
- On app restart, the token is automatically loaded — no re-login required
- Clicking **Log Out** removes the token from the Preferences store

---

## Implemented Features

### Mandatory Modules
| # | Module | Backend Endpoints Used |
|---|---|---|
| 1 | Authentication (Login, Register, Logout) | `POST /user`, `POST /authentication` |
| 2 | Exchange Rate Dashboard | `GET /exchangeRate`, `GET /analytics` |
| 3 | Exchange Rate Graph | `GET /exchangeRateHistory` |
| 4 | Transactions (Add + View History) | `POST /transaction`, `GET /transaction` |
| 5 | Export Transactions (CSV) | `GET /export` |

### Optional Modules
| # | Module | Backend Endpoints Used |
|---|---|---|
| 6 | P2P Marketplace | `GET/POST /market/offers`, `POST /market/offers/{id}/accept`, `DELETE /market/offers/{id}`, `GET /market/trades` |
| 7 | Alerts | `POST /alerts`, `GET /alerts`, `DELETE /alerts/{id}`, `GET /alerts/check` |

---

## Error Handling

| HTTP Code | Meaning | App Behavior |
|---|---|---|
| 400 | Bad Request / Validation | Error message shown near the form |
| 401 | Unauthorized | "Unauthorized. Please log in again." shown on screen |
| 403 | Forbidden | Descriptive message e.g. "You can only cancel your own offers" |
| 404 | Not Found | "Not found" message shown |
| 429 | Rate Limited | "Too many requests. Wait and try again." shown; button disabled |
| 500 | Server Error | Generic "Try again later" message shown |
| Network failure | Backend unreachable | "Network error: is the backend running?" shown |

---

## Dependencies (pom.xml excerpt)

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>22.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>22.0.1</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.retrofit2</groupId>
        <artifactId>retrofit</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.retrofit2</groupId>
        <artifactId>converter-gson</artifactId>
        <version>2.9.0</version>
    </dependency>
</dependencies>
```

---

## Troubleshooting

**App won't start — "Exception in Application start method"**
→ Make sure the backend is running at `http://localhost:5000` before starting the app.

**"No module found" / ClassNotFoundException**
→ Do **Build → Clean Project** then **Build → Rebuild Project** in IntelliJ.

**Charts not rendering / empty**
→ Ensure there is transaction data in the database for the selected date range.

**Token not persisting across restarts**
→ Check that the Java Preferences API has write permission. On Windows, run IntelliJ as normal user (not administrator) to use the correct registry hive.

**429 errors immediately**
→ The backend rate limits login to 5/min and transactions to 5/min. Wait 60 seconds and try again.
