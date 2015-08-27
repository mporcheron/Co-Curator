<?php

require_once 'db.php';
require_once 'updateUser.php';

\header('Content-type: application/json');

if ($stmt = $db->prepare('SELECT `globalUserId`, `userId`, `ip`, `userLastCheckin` FROM `user` WHERE `groupId`=:groupId')) {
	$stmt->bindParam(':groupId', $groupId, SQLITE3_INTEGER);
	
	if($res = $stmt->execute()) {
		$data = [];
		while($row = $res->fetchArray()) {
			if($row['userLastCheckin'] != '' && $row['userLastCheckin'] >= \time() - USER_LOGGED_IN_FOR) {
				$data[] = ['globalUserId' => $row['globalUserId'], 'userId' => $row['userId'], 'ip' => $row['ip']];
			}
		}

		$stmt->close();
	} else {
		\dieError($db->lastErrorMsg(), 'Internal Server Error');
	}
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}

\dieResult($data);