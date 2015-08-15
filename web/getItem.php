<?php

require_once 'db.php';

\requireInput('Must provide global user ID and item ID', ['globalUserId', 'itemId']);

$globalUserId = \intval($data['globalUserId']);
$localItemId = \intval($data['itemId']);

if ($stmt = $db->prepare('SELECT `localItemId`, `itemType`, `itemData`, `itemDateTime` FROM `item` WHERE `globalUserId`=:globalUserId AND `localItemId`=:localItemId')) {
	$stmt->bindParam(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	$stmt->bindParam(':localItemId', $localItemId, SQLITE3_INTEGER);

	if($res = $stmt->execute()) {
		$data = [];
		if($row = $res->fetchArray()) {
			$row['itemData'] = \removeUnicodeSequences($row['itemData']);

			$data = ['id' => $row['localItemId'],
				'type' => $row['itemType'],
				'data' => $row['itemData'],
				'dateTime' => $row['itemDateTime']];
		}

		if(empty($data)) {
			\dieError('Could not find [' . $globalUserId  .':'. $itemId .'] as requested by User[' . $globalUserId .']',
				'No matching item found.');
		}

		$stmt->close();
	} else {
		\dieError($db->lastErrorMsg(), 'Internal Server Error');
	}

	\dieResult($data);
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}
