<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';


$query = "SELECT sum(jumlah_bayar) AS total_kas FROM kas";
$result = mysqli_query($conn, $query);
$response = array();

//$server_name = '10.0.2.2';
$server_name = '192.168.43.209';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'total_kas'        =>$row['total_kas'])
    );
}

echo json_encode($response);

mysqli_close($conn);




?>