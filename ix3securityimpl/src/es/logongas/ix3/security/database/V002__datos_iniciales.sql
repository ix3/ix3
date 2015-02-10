INSERT INTO `sec_identity` (`ididentity`, `login`, `name`) VALUES
	(1, 'All', 'Todos'),
	(2, 'Authenticated', 'Autenticados');

INSERT INTO `sec_user` (`ididentity`) VALUES
	(1),
        (2);

INSERT INTO `sec_secureresourcetype` (`idSecureResourceType`, `name`, `description`) VALUES
	(1, 'URL', 'URL');
INSERT INTO `sec_permission` (`idPermission`, `name`, `description`, `idSecureResourceType`) VALUES
	(1, 'GET', 'GET', 1),
	(2, 'POST', 'POST', 1),
	(3, 'PUT', 'PUT', 1),
	(4, 'DELETE', 'DELETE', 1);


INSERT INTO `sec_secureresourcetype` (`idSecureResourceType`, `name`, `description`) VALUES
	(2, 'Entity', 'Entidad de negocio');
INSERT INTO `sec_permission` (`idPermission`, `name`, `description`, `idSecureResourceType`) VALUES
	(5, 'Read', 'Read', 2),
	(6, 'Insert', 'Insert', 2),
	(7, 'Update', 'Update', 2),
	(8, 'Delete', 'Delete', 2);