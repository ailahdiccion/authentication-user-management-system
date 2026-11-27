# Authentication and User Management System

A Java-based web application designed to manage user authentication, account administration, role-based access control, and administrative PDF reporting.

Developed as a collaborative academic partner project under the BS Computer Science program at the University of Santo Tomas.

## Key Features

- User authentication and login verification
- Role-based access control
- User account management
- Create, Read, Update, and Delete (CRUD) operations
- CAPTCHA verification
- Session management
- PDF report generation
- Database integration

## Technology Stack

| Category | Technologies |
| --- | --- |
| Backend | Java, Java Servlets, JDBC |
| Frontend | JSP, HTML, CSS, JavaScript |
| Database | Apache Derby |
| Application Server | GlassFish |
| Development Environment | Apache NetBeans |
| Reporting | iText PDF |

## My Role and Contributions

**Backend-focused Developer | Academic Partner Project**

Worked primarily on backend development as part of a two-person software development team.

This repository showcases our collaborative implementation and my experience contributing to a Java-based authentication and user management application.

## Project Structure

- `src/` - Java application source code
- `web/` - Web interfaces and application resources
- `nbproject/` - NetBeans project configuration
- `build.xml` - Ant build configuration

## Local Setup

1. Open the project in Apache NetBeans with a compatible Java and GlassFish environment.
2. Copy `web/WEB-INF/web.xml.example` to `web/WEB-INF/web.xml`.
3. Configure the required local database and application settings.
4. Restore the required libraries in `web/WEB-INF/lib/`.
5. Resolve application server and library references in NetBeans.
6. Create a local Apache Derby database named `LoginDB`.
7. Create a `USERS` table containing the `Email`, `Password`, and `UserRole` columns.
8. Create local demonstration accounts for testing.

**Note:** The original project archive does not contain a verified database schema or seed script. Do not commit local configuration files or database credentials.

### Required Dependencies

- `derbyclient.jar`
- `itextpdf-5.5.13.5.jar`

## Project Status and Limitations

This application was developed for academic purposes and is not intended for production deployment.

The portfolio version has not undergone full runtime testing or a comprehensive security audit.

The original legacy password encryption implementation is retained and should be replaced with secure password hashing before production use. The existing implementation also exposes decrypted passwords in certain reports and user interfaces.

Use fictional demonstration accounts only. Do not enter real credentials or sensitive personal information.

## Acknowledgments

Developed collaboratively as an academic partner project under the BS Computer Science program at the University of Santo Tomas. Credit belongs to both contributing developers.

### Team Members

- Diccion, Ma. Ailah Jolie
- Santiago, Clarissa Andrea