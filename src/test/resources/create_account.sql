
CREATE TABLE Accounts (
	id int(11) NOT NULL IDENTITY(1000, 1) PRIMARY KEY,
	email varchar(100) NOT NULL UNIQUE,
	password varchar(100) NOT NULL,
	fullname nvarchar2(200) NOT NULL,
	nickname nvarchar2(50) NOT NULL UNIQUE,
	latestlogin timestamp NOT NULL
);

CREATE TABLE Tokens (
    Token char(100) NOT NULL PRIMARY KEY,
    AccountId int(11) NOT NULL,
    ExpiryDate timestamp NOT NULL
)
