<?php

require_once 'db.php';
require_once 'updateUser.php';

\header('Content-type: application/json');

if ($stmt = $db->prepare('SELECT `userId` FROM `user` WHERE `globalUserId`=:globalUserId')) {
	$stmt->bindParam(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	
	if($res = $stmt->execute()) {
		$data = [];
		if ($row = $res->fetchArray()) {
			$data['success'] = 'A-OK';
			$data['userId'] = $row['userId'];
			$data['colour'] = $row['userId'];
		}

		$stmt->close();

		\dieResult($data);
	} else {
		\dieError($db->lastErrorMsg(), 'Internal Server Error');
	}
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}

\dieError($db->lastErrorMsg(), 'Internal Server Error');
