<?php 

header("Content-Type: application/json; charset=UTF-8");
require_once 'connect.php';

$query = "SELECT * FROM kegiatan ORDER BY id_kegiatan DESC ";
$result = mysqli_query($conn, $query);
$response = array();

//$server_name = $_SERVER['SERVER_ADDR'];
$server_name = '192.168.43.209';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'id_kegiatan'       =>$row['id_kegiatan'], 
        'nama_kegiatan'     =>$row['nama_kegiatan'], 
        'tgl_kegiatan'      =>$row['tgl_kegiatan']) 
    );
}

echo json_encode($response);

mysqli_close($conn);

?>