<?php

require_once 'db.php';

\header('Content-type: application/json');

\requireInput('Must provide user credentials', ['globalUserId']);
\requireInput('Must provide item details', ['id']);

$globalUserId = \intval($data['globalUserId']);
$itemId = \intval($data['id']);

if ($stmt = $db->prepare('UPDATE `item` SET `itemDeleted`=1 WHERE `globalUserId`=:globalUserId AND `localItemId`=:localItemId')) {
	$stmt->bindParam(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	$stmt->bindParam(':localItemId', $itemId, SQLITE3_INTEGER);

	if(!$stmt->execute()) {
		\dieError($db->lastErrorMsg(), 'Could not delete item in the cloud');
	}

	$stmt->close();
	\dieResult(['success' => 'A-OK']);
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}
