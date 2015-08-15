<?php

require_once 'db.php';
require_once 'updateUser.php';

if ($stmt = $db->prepare('SELECT `globalUserId`, `userId`, `ip` FROM `user` WHERE `groupId`=:groupId')) {
	$stmt->bindParam(':groupId', $groupId, SQLITE3_INTEGER);
	
	if($res = $stmt->execute()) {
		$data = [];
		while($row = $res->fetchArray()) {
			$data[] = ['globalUserId' => $row['globalUserId'], 'ip' => $row['ip']];
		}

		$stmt->close();
	} else {
		\dieError($db->lastErrorMsg(), 'Internal Server Error');
	}
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}

\dieResult($data);