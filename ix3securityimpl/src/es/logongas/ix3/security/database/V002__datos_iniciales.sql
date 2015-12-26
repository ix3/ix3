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
	(4, 'DELETE', 'DELETE', 1),
	(5, 'PATCH', 'PATCH', 1);


INSERT INTO `sec_secureresourcetype` (`idSecureResourceType`, `name`, `description`) VALUES
	(2, 'Service', 'Servicio');
INSERT INTO `sec_permission` (`idPermission`, `name`, `description`, `idSecureResourceType`) VALUES
	(20, 'PreExecuteService', 'Pre-Execute Service', 2),
	(21, 'PostExecuteService', 'Post-Execute Service', 2);

INSERT INTO `sec_secureresourcetype` (`idSecureResourceType`, `name`, `description`) VALUES
	(3, 'Controller', 'Controladores públicos');
INSERT INTO `sec_permission` (`idPermission`, `name`, `description`, `idSecureResourceType`) VALUES
	(22, 'PreExecuteController', 'Pre-Execute Controller', 3),
	(23, 'PostExecuteController', 'Post-Execute Controller', 3);