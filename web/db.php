<?php

\define('DB_NAME', 'cocurator');

\define('IMG_DIR', \dirname(__FILE__) . '/uploads/');
\define('IMG_WWW', 'http://'. $_SERVER['SERVER_ADDR'] . \dirname($_SERVER['REQUEST_URI']) . '/uploads/');

\define('ERROR_LOG', \dirname(__FILE__) .'/error.log');

$data = $_REQUEST;


//


$db = new SQLite3(DB_NAME . '.db');


function requireInput($mesg, array $keys) {
	global $data;
	foreach ($keys as $key) {
		if (!isset($data[$key]) || ($data[$key] != 0 && empty($data[$key]))) {
			die(\json_encode(['error' => $mesg]));
		}
	}
}

function removeUnicodeSequences($struct) {
   return preg_replace('/[^\x00-\x7f]/', '', $struct);
}

function errorLog($mesg) {
	\file_put_contents(ERROR_LOG, \date('c') . "\t" . $mesg . "\n", FILE_APPEND);
}

function dieError($logError, $userError) {
	\errorLog($logError);
	\dieResult(['error' => $userError]);
}

function dieResult($data) {
	die(\json_encode($data));
}


//


\header('Content-type: application/json');