-- schema.sql
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    age INT,
    mobile VARCHAR(15),
    address TEXT,
    occupation VARCHAR(100),
    account_id VARCHAR(10) UNIQUE NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- NEW COLUMN: Tracks Opening Date
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    account_id VARCHAR(10) NOT NULL,
    type VARCHAR(20),
    amount DECIMAL(15, 2),
    details VARCHAR(255),
    related_name VARCHAR(100),
    related_account_id VARCHAR(50),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY(account_id) REFERENCES users(account_id) ON DELETE CASCADE
);

-- DEFAULT ADMIN USER
INSERT INTO users (name, username, password, age, mobile, address, occupation, account_id, balance, role)
VALUES ('Head Manager', 'admin', 'admin123', 40, '0000000000', 'Bank HQ', 'Bank Manager', 'ADMIN001', 0.00, 'EMPLOYEE');
-- DEFAULT ADMIN USER (Username: admin, Password: admin123)
-- He has a special role 'EMPLOYEE'
INSERT INTO users (name, username, password, age, mobile, address, occupation, account_id, balance, role)
VALUES ('Head Manager', 'admin', 'admin123', 40, '0000000000', 'Bank HQ', 'Bank Manager', 'ADMIN001', 0.00, 'EMPLOYEE');
