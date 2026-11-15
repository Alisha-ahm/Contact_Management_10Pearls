-- ============================================================================
-- Contact Management System - Microsoft SQL Server Database Schema
-- Compatible with Spring Data JPA & Hibernate 6
-- ============================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ContactManagementDb')
BEGIN
    CREATE DATABASE ContactManagementDb;
END
GO

USE ContactManagementDb;
GO

IF OBJECT_ID('dbo.contact_phones', 'U') IS NOT NULL DROP TABLE dbo.contact_phones;
IF OBJECT_ID('dbo.contact_emails', 'U') IS NOT NULL DROP TABLE dbo.contact_emails;
IF OBJECT_ID('dbo.contacts', 'U') IS NOT NULL DROP TABLE dbo.contacts;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

CREATE TABLE dbo.users (
    id NVARCHAR(64) NOT NULL PRIMARY KEY,
    full_name NVARCHAR(120) NOT NULL,
    email NVARCHAR(255) NULL,
    phone NVARCHAR(50) NULL,
    company NVARCHAR(150) NULL,
    role NVARCHAR(50) NOT NULL CONSTRAINT DF_Users_Role DEFAULT 'Standard User',
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSDATETIMEOFFSET(),
    last_login DATETIMEOFFSET NOT NULL CONSTRAINT DF_Users_LastLogin DEFAULT SYSDATETIMEOFFSET()
);
GO

CREATE UNIQUE NONCLUSTERED INDEX UX_Users_Email ON dbo.users(email) WHERE email IS NOT NULL;
CREATE UNIQUE NONCLUSTERED INDEX UX_Users_Phone ON dbo.users(phone) WHERE phone IS NOT NULL;
GO

CREATE TABLE dbo.contacts (
    id NVARCHAR(64) NOT NULL PRIMARY KEY,
    user_id NVARCHAR(64) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    title NVARCHAR(150) NULL,
    company NVARCHAR(150) NULL,
    department NVARCHAR(100) NULL,
    address NVARCHAR(255) NULL,
    city NVARCHAR(100) NULL,
    state NVARCHAR(100) NULL,
    country NVARCHAR(100) NULL,
    category NVARCHAR(50) NOT NULL CONSTRAINT DF_Contacts_Category DEFAULT 'General',
    is_favorite BIT NOT NULL CONSTRAINT DF_Contacts_Favorite DEFAULT 0,
    avatar_color NVARCHAR(30) NULL,
    notes NVARCHAR(MAX) NULL,
    created_at DATETIMEOFFSET NOT NULL CONSTRAINT DF_Contacts_CreatedAt DEFAULT SYSDATETIMEOFFSET(),
    updated_at DATETIMEOFFSET NOT NULL CONSTRAINT DF_Contacts_UpdatedAt DEFAULT SYSDATETIMEOFFSET(),
    CONSTRAINT FK_Contacts_Users FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_Contacts_User_Name ON dbo.contacts(user_id, last_name, first_name);
GO

CREATE TABLE dbo.contact_emails (
    id NVARCHAR(64) NOT NULL PRIMARY KEY,
    contact_id NVARCHAR(64) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    label NVARCHAR(30) NOT NULL CONSTRAINT CK_ContactEmails_Label CHECK (label IN ('work', 'personal', 'other')),
    is_primary BIT NOT NULL CONSTRAINT DF_ContactEmails_Primary DEFAULT 0,
    CONSTRAINT FK_ContactEmails_Contacts FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_ContactEmails_ContactId ON dbo.contact_emails(contact_id);
GO

CREATE TABLE dbo.contact_phones (
    id NVARCHAR(64) NOT NULL PRIMARY KEY,
    contact_id NVARCHAR(64) NOT NULL,
    phone_number NVARCHAR(60) NOT NULL,
    label NVARCHAR(30) NOT NULL CONSTRAINT CK_ContactPhones_Label CHECK (label IN ('work', 'mobile', 'home', 'personnel', 'other')),
    is_primary BIT NOT NULL CONSTRAINT DF_ContactPhones_Primary DEFAULT 0,
    CONSTRAINT FK_ContactPhones_Contacts FOREIGN KEY (contact_id) REFERENCES dbo.contacts(id) ON DELETE CASCADE
);
GO

CREATE NONCLUSTERED INDEX IX_ContactPhones_ContactId ON dbo.contact_phones(contact_id);
GO
