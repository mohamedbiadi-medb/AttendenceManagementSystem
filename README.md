# Attendance Management System v5

Features:
- Dashboard with today's dynamic date
- Dashboard starts at 0 until attendance is actually saved
- Attendance score starts at 100%
- Absent = -5 points
- Late = -2 points
- Score never goes below 0%
- Progress bar for each student
- Required comment when marking a student Absent
- Comments saved in attendance.csv
- Student Report filtered by student (no date filter)
- Daily Attendance PDF export from Dashboard
- CSV storage only for app data; PDF reports are stored under data/reports
- No database

## IntelliJ
Open the folder as a Maven project. Use JDK 17 or newer.
When IntelliJ detects the new OpenPDF dependency, let Maven download it.

## CSV
attendance.csv now uses:
date,student_id,name,status,comment

The program can still read older 4-column attendance files; old records simply have an empty comment.
