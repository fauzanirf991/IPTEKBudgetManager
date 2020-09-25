<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$query = "SELECT * FROM aset ORDER BY id_aset DESC ";
$result = mysqli_query($conn, $query);
$response = array();

//$server_name = '10.0.2.2';

$server_name = '192.168.43.209';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'id_aset'        =>$row['id_aset'], 
        'nama_aset'      =>$row['nama_aset'], 
        'harga_aset'     =>$row['harga_aset'],
        'tgl_aset'     =>date('d M Y', strtotime($row['tgl_aset'])),
        'bukti'   =>"http://$server_name" . $row['bukti']) 
    );
}

echo json_encode($response); 

mysqli_close($conn);


?>