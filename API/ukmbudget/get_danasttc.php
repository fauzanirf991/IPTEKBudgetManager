<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';


$query = "SELECT jumlahdana FROM pagudanastatic WHERE id_pagudanastc = 1";
$result = mysqli_query($conn, $query);
$response = array();

//$server_name = '10.0.2.2';
$server_name = '192.168.43.209';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'jumlahdana'        =>$row['jumlahdana'])
    );
}

echo json_encode($response);

mysqli_close($conn);




?>