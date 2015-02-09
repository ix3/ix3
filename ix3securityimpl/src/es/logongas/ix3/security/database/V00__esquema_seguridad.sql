CREATE TABLE `sec_identity` (
  `ididentity` int(11) NOT NULL AUTO_INCREMENT,
  `login` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ididentity`),
  UNIQUE KEY `login` (`login`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=50;


CREATE TABLE `sec_user` (
  `ididentity` int(11) NOT NULL,
  PRIMARY KEY (`ididentity`),
  KEY `FK285FEB1EDD9A75` (`ididentity`),
  CONSTRAINT `FK285FEB1EDD9A75` FOREIGN KEY (`ididentity`) REFERENCES `sec_identity` (`ididentity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `sec_group` (
  `ididentity` int(11) NOT NULL,
  PRIMARY KEY (`ididentity`),
  KEY `FK41E065F1EDD9A75` (`ididentity`),
  CONSTRAINT `FK41E065F1EDD9A75` FOREIGN KEY (`ididentity`) REFERENCES `sec_identity` (`ididentity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sec_groupmember` (
  `idGroupMember` int(11) NOT NULL AUTO_INCREMENT,
  `idGroup` int(11) DEFAULT NULL,
  `ididentity` int(11) DEFAULT NULL,
  `priority` int(11) DEFAULT NULL,
  PRIMARY KEY (`idGroupMember`),
  KEY `FK8598F9D9AAEDEABC` (`idGroup`),
  KEY `FK8598F9D91EDD9A75` (`ididentity`),
  CONSTRAINT `FK8598F9D91EDD9A75` FOREIGN KEY (`ididentity`) REFERENCES `sec_identity` (`ididentity`),
  CONSTRAINT `FK8598F9D9AAEDEABC` FOREIGN KEY (`idGroup`) REFERENCES `sec_group` (`ididentity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;





CREATE TABLE `sec_secureresourcetype` (
  `idSecureResourceType` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idSecureResourceType`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sec_secureresource` (
  `idSecureResource` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `idSecureResourceType` int(11) DEFAULT NULL,
  PRIMARY KEY (`idSecureResource`),
  UNIQUE KEY `name_idSecureResourceType` (`name`,`idSecureResourceType`),
  KEY `FK920CE8C5850089C0` (`idSecureResourceType`),
  CONSTRAINT `FK920CE8C5850089C0` FOREIGN KEY (`idSecureResourceType`) REFERENCES `sec_secureresourcetype` (`idSecureResourceType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `sec_permission` (
  `idPermission` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `idSecureResourceType` int(11) DEFAULT NULL,
  PRIMARY KEY (`idPermission`),
  UNIQUE KEY `name_idSecureResourceType` (`name`,`idSecureResourceType`),
  KEY `FK57F7A1EF850089C0` (`idSecureResourceType`),
  CONSTRAINT `FK57F7A1EF850089C0` FOREIGN KEY (`idSecureResourceType`) REFERENCES `sec_secureresourcetype` (`idSecureResourceType`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sec_ace` (
  `idACE` int(11) NOT NULL AUTO_INCREMENT,
  `aceType` varchar(255) DEFAULT NULL,
  `idPermission` int(11) DEFAULT NULL,
  `ididentity` int(11) DEFAULT NULL,
  `secureResourceRegExp` varchar(255) DEFAULT NULL,
  `conditionalScript` text,
  `priority` int(11) DEFAULT NULL,
  PRIMARY KEY (`idACE`),
  KEY `FKFC63E44E74A0` (`idPermission`),
  KEY `FKFC631EDD9A75` (`ididentity`),
  CONSTRAINT `FKFC631EDD9A75` FOREIGN KEY (`ididentity`) REFERENCES `sec_identity` (`ididentity`),
  CONSTRAINT `FKFC63E44E74A0` FOREIGN KEY (`idPermission`) REFERENCES `sec_permission` (`idPermission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



INSERT INTO `sec_identity` (`ididentity`, `login`, `name`) VALUES
	(1, 'All', 'Todos'),
	(2, 'Authenticated', 'Autenticados');

INSERT INTO `sec_user` (`ididentity`) VALUES
	(1),
        (2);

