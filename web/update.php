<?php

require_once 'db.php';

\header('Content-type: application/json');

\requireInput('Must provide user credentials', ['globalUserId']);
\requireInput('Must provide item details', ['id','data']);

$globalUserId = \intval($data['globalUserId']);
$localItemId = \intval($data['id']);
$itemData = SQLite3::escapeString($data['data']);

$globalItemId = $globalUserId .':'. $itemId;

if ($stmt = $db->prepare('UPDATE `item` SET `itemData`=:itemData WHERE `globalUserId`=:globalUserId AND `localItemId`=:localItemId')) {
	$stmt->bindParam(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	$stmt->bindParam(':localItemId', $localItemId, SQLITE3_INTEGER);
	$stmt->bindParam(':itemData', $itemData, SQLITE3_TEXT);

	if(!$stmt->execute()) {
		\dieError($db->lastErrorMsg(), 'Could not update item in the cloud');
	}

	$stmt->close();
	\dieResult(['success' => 'A-OK']);
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}
