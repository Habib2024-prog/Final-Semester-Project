# Remote Patient Monitoring System (RPMS)

## Overview
The **Remote Patient Monitoring System (RPMS)** is a Java-based desktop healthcare application that enables remote monitoring of patients by doctors.  
The system supports:
- Patient vitals upload and analysis.
- Appointment and video consultation management.
- Real-time private chat using Socket API.
- Intelligent medication effectiveness tracking and vital trends visualization.
- Panic button for emergencies.
- Automatic reminders for appointments and medications.
- Admin management of users, reports, and the system.

---

## Features

- ✅ Secure login for Patients, Doctors, and Admin.
- ✅ Real-time chat (one-to-one) between patients and doctors using Socket API.
- ✅ Upload patient vitals via CSV.
- ✅ Automatic emergency detection and manual panic button.
- ✅ Appointment and medication reminders.
- ✅ Video consultation request, approval, and link management.
- ✅ System-managed reports and health trends visualization.
- ✅ Admin can manage doctors, patients, appointments, and reports.
- ✅ File-based data storage (no database needed).

---

## Project Structure
* RPMS/
* ├── src/
* │   └── main/
* │       └── java/
* │           ├── Chat/
* │           │   ├── ChatMessage.java
* │           │   ├── ChatServer.java  ✅ for Running chat need to run this 
* │           │   ├── DoctorChatPanel.java
* │           │   ├── DoctorListRenderer.java
* │           │   └── PatientChatPanel.java
* │           ├── DashBoard/
* │           ├── Database/
* │           ├── Model/
* │           │   ├── Admin.java
* │           │   ├── Doctors.java
* │           │   ├── LoginSystem.java  ✅ Start point of the system (GUI Login)
* │           │   ├── Patients.java
* │           │   └── User.java
* │           ├── services/
* │           ├── utill/
* │           └── Main/

---

## Getting Started

### 1️⃣ Start Chat Server (Required for Real-time Chat)
**Location:**  
`src/main/java/Chat/ChatServer.java`

- Run `ChatServer` first.
- This will open the chat server on `localhost:8080`.
- Keeps running in the background for real communication.

### 2️⃣ Start Login System (Main Application Launcher)
**Location:**  
`src/main/java/Model/LoginSystem.java`

- Run `LoginSystem`.
- The system presents a secure **Login screen**.
- Supports Admin, Doctor, and Patient login.
- If the user is not registered, they can register and the system will navigate to the respective dashboard.


### Technologies Used
* Java 24 (JDK 24)
* 
* Java Swing (GUI)
* 
* Maven Project (pom.xml managed)
* 
* Socket API (Chat Server/Client communication)
* 
* File-based data persistence (UserFile/)
* 
* JavaFX (Graphs, Reports)
* 
* iText (PDF generation)
* 
* Jakarta Mail (Reminders & notifications)

## Important Notes
#### ✅ Always start ChatServer before launching LoginSystem.

#### ✅ The system uses file-based data; make sure UserFile/ directory exists and has the right permissions.

#### ✅ Admin can manage all system entities and generate reports.

## Conclusion
1. [x] The RPMS successfully implements a complete, secure, and user-friendly remote health monitoring system.
2. [x] 
3. [x] Real-time Doctor-Patient communication is ensured via the Socket API.
4. [x] 
5. [x] The system ensures proactive health management with vitals trend analysis and medication effectiveness evaluation.
6. [x] 
7. [x] Emergency alerts, reminders, and video consultations ensure timely patient care.
8. [x] 
9. [x] The system allows for flexible file-based data storage, ensuring offline functionality as well.
10. [x] 
11. [x] The Admin can effectively control the system, users, appointments, and generate reports.
12. [x] 
13. [x] Overall, RPMS delivers a comprehensive and extensible remote patient monitoring solution.



