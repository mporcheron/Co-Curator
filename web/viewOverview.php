<?php

require_once 'db.php';

\requireInput('Must provide group ID', ['groupId']);

\define('TYPE_NOTE', 1);
\define('TYPE_PHOTO', 0);
\define('TYPE_URL', 2);

$groupId = \intval($data['groupId']);

$users = [];
if ($stmt = $db->prepare('SELECT `userId`, `globalUserId` FROM `user` WHERE `groupId`=:groupId')) {
	$stmt->bindParam(':groupId', $groupId, SQLITE3_INTEGER);

	if($res = $stmt->execute()) {
		while($row = $res->fetchArray()) {
			$users[$row['userId']] = $row['globalUserId'];
		}
	}
}

$query = 'SELECT `globalUserId`, `itemType`, `itemData`, `itemDateTime` FROM `item` WHERE (`globalUserId` = ?';
for ($i = 1; $i < count($users); $i++) {
	$query .= ' OR `globalUserId` = ?';
}
$query .= ') AND `itemDeleted` = 0 ORDER BY `itemDateTime` ASC;';


if ($stmt = $db->prepare($query)) {
	for ($i = 0; $i < count($users); $i++) {	
 		$stmt->bindValue($i+1, $users[$i], SQLITE3_INTEGER);
	}?>
<html><head><script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script><style>
html { margin: 0; padding: 0; }
body { margin: .5%; padding: 0; }
div {
	width: 150px;
	height: 150px;
	margin: .5%;
	background: #000000;
	font-family: Helvetica, sans-serif;
	font-size: 1.6em;
	float: left;
	display: block;
}
div img {
	height: 100%;
	width: 100%;
	object-position: center;
}
div.user0 { background-color: #7BBD31; color: #000; }
div.user1 { background-color: #1B8DC8; color: #FFF; }
div.user2 { background-color: #FC5C93; color: #FFF; }
div.user3 { background-color: #F6AC61; color: #000; }
@media print {
	html, body {
		margin: 0;
		width: 100%;
	}
    div {
    	width: 19%;
    }
}

</style><body><?php
	if ($res = $stmt->execute()) {
		while ($row = $res->fetchArray()) {
			$userId = \array_search($row['globalUserId'], $users);

			print '<div class="user'. $userId .'">';
			if ($row['itemType'] == TYPE_PHOTO) {
				print '<img src="uploads/'. $row['itemData'] .'">';
			} else if ($row['itemType'] == TYPE_NOTE) {
				print $row['itemData'];
			} else if ($row['itemType'] == TYPE_URL) {
				print '<img src="getScreenshot.php?url='. base64_encode($row['itemData']) .'">';
			}

			print '</div>';
		}

		$stmt->close();
	} else {
		\dieError($db->lastErrorMsg(), 'Internal Server Error');
	}
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}?>
<script>

</script>
</body></html>
