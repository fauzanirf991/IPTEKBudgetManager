<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];

if ( $key == "update" ){

    $id_kas        = $_POST['id_kas'];
    $nama_pembayar = $_POST['nama_pembayar'];
    $jumlah_bayar = $_POST['jumlah_bayar'];
    $tgl_bayar = $_POST['tgl_bayar'];

    $jumlah_bayar =(int)$jumlah_bayar;
    $tgl_bayar =  date('Y-m-d', strtotime($tgl_bayar));

    $query = "UPDATE kas SET 
    nama_pembayar='$nama_pembayar',  
    jumlah_bayar='$jumlah_bayar',
    tgl_bayar='$tgl_bayar'
    WHERE id_kas='$id_kas' ";

        if ( mysqli_query($conn, $query) ){
            $result["value"] = "1";
            $result["message"] = "Success!";
            
            echo json_encode($result);
            mysqli_close($conn);

            

        } 
        else {
            $response["value"] = "0";
            $response["message"] = "Error! ".mysqli_error($conn);
            echo json_encode($response);

            mysqli_close($conn);
        }
}



?>