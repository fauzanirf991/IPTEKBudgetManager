<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$query = "SELECT * FROM kas ORDER BY id_kas DESC ";
$result = mysqli_query($conn, $query);
$response = array();

$server_name = '10.0.2.2';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'id_kas'            =>$row['id_kas'], 
        'nama_pembayar'     =>$row['nama_pembayar'], 
        'jumlah_bayar'      =>$row['jumlah_bayar'],
        'tgl_bayar'         =>date('d M Y', strtotime($row['tgl_bayar'])))
    );
}

echo json_encode($response);

mysqli_close($conn);


?>