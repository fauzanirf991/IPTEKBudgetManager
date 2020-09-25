<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$id_kegiatan = $_POST['id_kegiatan'];
$id_kegiatan = (int)$id_kegiatan;
$query = "SELECT * FROM pengeluarankegiatan WHERE id_kegiatan=$id_kegiatan ORDER BY id_pengeluaran DESC ";
$result = mysqli_query($conn, $query);
$response = array();

//$server_name = '10.0.2.2';
$server_name = '192.168.43.209';

while( $row = mysqli_fetch_assoc($result) ){

    array_push($response, 
    array(
        'id_pengeluaran'        =>$row['id_pengeluaran'], 
        'nama_pengeluaran'      =>$row['nama_pengeluaran'], 
        'jumlah_item'           =>$row['jumlah_item'],
        'harga_satuan'          =>$row['harga_satuan'],
        'tgl_pengeluaran'       =>date('d M Y', strtotime($row['tgl_pengeluaran'])),
        'bukti'                 =>"http://$server_name" . $row['bukti'],
        'id_kegiatan'           =>$row['id_kegiatan'],
        'nama_kegiatan'         =>$row['id_kegiatan']) 
    );
}

echo json_encode($response);

mysqli_close($conn);


?>