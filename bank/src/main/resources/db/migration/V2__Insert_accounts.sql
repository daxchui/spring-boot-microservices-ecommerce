-- Insert Store account (ID=1)
INSERT INTO account (owner_name, currency, balance) 
VALUES ('Store Account', 'AUD', 0.00);

-- Insert test customer accounts (ID=2 through 6)
INSERT INTO account (owner_name, currency, balance) VALUES
('Customer 1', 'AUD', 10000.00),
('Customer 2', 'AUD', 10000.00),
('Customer 3', 'AUD', 10000.00),
('Customer 4', 'AUD', 10000.00),
('Customer 5', 'AUD', 10000.00);
