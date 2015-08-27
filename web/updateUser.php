<?php

require_once 'db.php';

\header('Content-type: application/json');

\requireInput('Must provide user credentials', ['globalUserId', 'groupId']);
\requireInput('Must provide IP address', ['ip']);

$globalUserId = \intval($data['globalUserId']);
$groupId = \intval($data['groupId']);
$ip = SQLite3::escapeString($data['ip']);

if ($stmt = $db->prepare('UPDATE `user` SET `ip`=:ip WHERE `globalUserId`=:globalUserId AND `groupId`=:groupId')) {
	$stmt->bindParam(':ip', $ip, SQLITE3_TEXT);
	$stmt->bindParam(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	$stmt->bindParam(':groupId', $groupId, SQLITE3_INTEGER);

	if(!$stmt->execute()) {
		\dieError($db->lastErrorMsg(), 'Could not register user with the cloud');
	}

	$stmt->close();
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}
