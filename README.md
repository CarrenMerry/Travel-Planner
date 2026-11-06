# Wayfarer — Smart Travel Planner & Route Optimizer

Wayfarer is a clean, modern, and accessible travel planning web application built with **pure Java 17**, **Apache Tomcat 10.1 (Embedded)**, **MySQL 8.0+**, **JDBC**, and a responsive **HTML5/CSS3/Vanilla JavaScript** frontend.

The project is intentionally streamlined to deliver a real-world travel product while remaining simple and understandable for academic project demonstrations.

---

## 1. System Architecture & Tech Stack

```text
+---------------------------------------------------------------------------------+
|                       WAYFARER USER INTERFACE (HTML5/CSS3/ES6)                  |
|  - Modern Editorial Design System: Warm off-white, Forest Green, Amber accents  |
|  - 8 Clean Pages: Landing, Login/Register, Dashboard, Explore, My Itineraries,  |
|    Itinerary Builder, Route Finder (BFS), Admin Console                         |
+---------------------------------------------------------------------------------+
                                      |  REST JSON (Fetch API)
                                      v
+---------------------------------------------------------------------------------+
|                       JAKARTA SERVLETS (Tomcat 10.1 Embedded)                   |
|  - AuthServlet        (/api/auth/*)         - Session RBAC & BCrypt Auth        |
|  - DestinationServlet (/api/destinations/*) - Destination REST operations       |
|  - ConnectionServlet  (/api/connections/*)  - Transit graph edges               |
|  - ItineraryServlet   (/api/itineraries/*)  - Atomic stop sequence & reorder    |
|  - RouteServlet       (/api/routes/*)       - BFS fewest-hop route finder       |
|  - AdminServlet       (/api/admin/*)        - Admin reporting & analytics       |
+---------------------------------------------------------------------------------+
                                      |
         +----------------------------+----------------------------+
         |                                                         |
         v                                                         v
+----------------------------------+             +----------------------------------+
|      ESSENTIAL DATA STRUCTURES   |             |     PERSISTENCE & DAO LAYER      |
|  - CustomLinkedList<T>           |             |  - DatabaseConnection (JDBC Pool)|
|    (Node, moveUp, moveDown)      |             |  - DestinationDAO                |
|  - TravelGraph (Adjacency List)  |             |  - ConnectionDAO                 |
|  - BFSRouteFinder (Queue BFS)    |             |  - ItineraryDAO & ItineraryStopDAO|
+----------------------------------+             |  - UserDAO                       |
                                                 +----------------------------------+
                                                                   |
                                                                   v
                                                 +----------------------------------+
                                                 |   MySQL: smart_travel_planner    |
                                                 |  - users (BCrypt + SHA-256)      |
                                                 |  - destinations (with category)  |
                                                 |  - travel_connections            |
                                                 |  - itineraries                   |
                                                 |  - itinerary_stops               |
                                                 +----------------------------------+
```

### Technology Matrix
- **Runtime:** Java 17 (Microsoft OpenJDK)
- **Web Container:** Apache Tomcat 10.1.20 (Embedded)
- **Database:** MySQL 8.0+ (`smart_travel_planner`) via standard JDBC
- **Password Security:** `org.mindrot:jbcrypt:0.4` + automatic legacy hash upgrade
- **JSON Serialization:** Google Gson 2.10.1
- **Testing:** JUnit 5 (`junit-jupiter-api`, `junit-jupiter-engine`) — **33/33 tests passing**
- **Frontend:** Semantic HTML5, Vanilla CSS3 (Custom Design System, CSS Variables, Responsive Grid/Flexbox), Vanilla ES6 JavaScript (No heavy frameworks required)

---

## 2. Essential Academic Data Structures & Algorithms

To prioritize code clarity, understanding, and explainability during college demonstrations, the application implements **two foundational DSA concepts**:

### I. Custom Linked List (`com.wayfarer.ds.CustomLinkedList<T>`)
- **Role:** Models the sequential, ordered nature of itinerary stops.
- **Data Structure:** Singly linked list with `head` and `tail` pointers.
- **Operations & Complexity:**
  - `add(T element)`: $O(1)$ time complexity using the maintained tail pointer.
  - `insert(int index, T element)`: $O(N)$ index traversal and node splicing.
  - `remove(int index)`: $O(N)$ traversal with pointer rewiring.
  - `moveUp(int index)`: $O(N)$ swaps stop with predecessor.
  - `moveDown(int index)`: $O(N)$ swaps stop with successor.
  - `toList()`: $O(N)$ linear traversal converting linked nodes to list for JSON serialization and atomic database persistence.
- **Integration:** Directly drives the **Itinerary Builder**. Clicking the **Up (↑)** and **Down (↓)** buttons re-links nodes in the in-memory linked list and atomically saves the new stop order to the MySQL database.

### II. Graph & Breadth-First Search (`com.wayfarer.ds.TravelGraph` & `BFSRouteFinder`)
- **Role:** Models the global transit network. Vertices represent destinations; directed edges represent travel routes.
- **Representation:** Adjacency List `Map<String, List<Edge>>`.
- **Breadth-First Search (BFS):**
  - Traverses the graph level-by-level using an explicit FIFO Queue and Visited Set.
  - Mathematical Guarantee: In an unweighted graph, BFS finds the route with the **minimum number of transit connections (fewest hops)**.
  - Complexity: Time $O(V + E)$, Space $O(V)$.
- **Integration:** Powers the **Route Finder (BFS)** page. Clearly shows destination cards in traversal order, connection metrics, and explicitly clarifies that BFS optimizes for minimum transit hops.

---

## 3. Database Preservation & Schema

The application connects directly to the existing MySQL database:
- **Database Name:** `smart_travel_planner`
- **Configuration File:** `db.properties`
- **Data Preservation:** All 5 existing tables (`users`, `destinations`, `travel_connections`, `itineraries`, `itinerary_stops`) and existing rows are preserved intact.
- **Transactions:** `ItineraryStopDAO` uses `conn.setAutoCommit(false)` and batch operations to ensure atomic stop sequence updates.

---

## 4. The 8 Essential Pages

| Page | URL | Description |
|---|---|---|
| **Landing Page** | `http://localhost:8080/index.html` | Brand hero section, how it works, and live destination previews from MySQL |
| **Sign In & Register** | `http://localhost:8080/login.html` | Compact auth card with Sign In and Registration tabs, validation, and password visibility toggle |
| **Traveler Dashboard** | `http://localhost:8080/dashboard.html` | Personalized greeting, trip count summary, quick actions, and recent itineraries |
| **Explore Destinations** | `http://localhost:8080/explore.html` | Search bar, category filter pills, destination cards with "Add to Itinerary" modal |
| **My Itineraries** | `http://localhost:8080/itineraries.html` | Table of user trips, stop count, budget, estimated cost, status badge, and deletion confirmation |
| **Itinerary Builder** | `http://localhost:8080/itinerary-builder.html` | Step-by-step stop organizer with **Custom Linked List** Move Up (↑), Move Down (↓), and Remove (✕) controls |
| **Route Finder (BFS)** | `http://localhost:8080/route-planner.html` | Origin/target dropdowns, swap button, BFS execution, step cards, and hop count |
| **Admin Console** | `http://localhost:8080/admin.html` | Protected management dashboard for destination CRUD and graph transit connection edges |

---

## 5. Demo Accounts & Credentials

| Role | Email Address | Password | Permissions |
|---|---|---|---|
| **Administrator** | `admin@travelplanner.com` | `admin123` | Full access to Admin Console, destination CRUD, and graph transit edges |
| **Traveler (User)** | `traveler@travelplanner.com` | `travel123` | Access to Traveler Dashboard, My Itineraries, Itinerary Builder, and Route Finder |

*(New user accounts registered via `/login.html` are automatically hashed with **BCrypt** and assigned the `USER` role).*

---

## 6. How to Run Locally

### Prerequisites
- Java 17+ installed (`java -version`)
- Apache Maven 3.9+ (`mvn -version`)
- MySQL Server running on `localhost:3306` with database `smart_travel_planner`

### Step 1: Run Automated Tests
```bash
mvn test
```
*Expected: `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`*

### Step 2: Start Embedded Tomcat
```bash
mvn exec:java
```
Once initialized, open your browser and navigate to:
```text
http://localhost:8080
```
