# NexGen Care - Hospital Management System

A Java Swing desktop application for managing doctors, patient appointments and billing in a hospital. Built as an object-oriented project using core Java.

## Features

- **Colour-coded appointment slots**: green means available, red means booked
- **Double-booking prevention**: a doctor's time slot cannot be booked twice
- **Live dashboard**: active doctors, today's appointments, total patients and today's revenue update automatically
- **Patient search**: find records by patient name or Patient ID (partial and case-insensitive), and delete a record straight from the results
- **Appointment management**: reschedule or cancel an appointment with one click on a booked slot
- **Billing**: generate a receipt for each appointment and export it as a `.txt` file
- **Doctor management**: add doctors with a specialization (20+ options or a custom one) and flexible time slots, or deactivate them
- **Data persistence**: all doctors and appointments are saved between sessions using Java serialization
- **Login screen** to access the system

## OOP Concepts Used

- **Inheritance**: `Doctor` and `Patient` extend `Person`
- **Interface**: `Billable` is implemented by `Patient`
- **Polymorphism**: `getDetails()` is overridden in `Doctor` and `Patient`
- **Encapsulation**: private fields with getters and setters
- **Serialization**: `Serializable` classes saved to `hms_data.ser`

## Technologies

- Java
- Java Swing (GUI)
- Java Serialization (data storage)

## How to Run

1. Make sure the JDK is installed.
2. Download `NexGenCareHMS.java`.
3. Open a terminal in the same folder and run:

```
javac NexGenCareHMS.java
java NexGenCareHMS
```

4. Log in with:

```
Username: admin
Password: admin
```

## Project Structure

| Class | Purpose |
|-------|---------|
| `Person` | Base class with a name |
| `Doctor` | Doctor with specialization and time slots |
| `Patient` | Patient with a Patient ID and billing |
| `Billable` | Interface for bill calculation |
| `Appointment` | Links a patient, doctor, time and date |
| `LoginScreen` | Login window |
| `HospitalManagementSystem` | Main window and application logic |

## Screenshots

*Add screenshots here.*
