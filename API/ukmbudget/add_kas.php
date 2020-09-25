<?php

require_once 'connect.php';

$key = $_POST['key'];

$nama_pembayar = $_POST['nama_pembayar'];
$jumlah_bayar = $_POST['jumlah_bayar'];
$tgl_bayar = $_POST['tgl_bayar'];



if ( $key == "insert" ){

    $tgl_bayar = date('Y-m-d', strtotime($tgl_bayar));
    $jumlah_bayar =(int)$jumlah_bayar;

    $query = "INSERT INTO kas(nama_pembayar, jumlah_bayar, tgl_bayar) 
    VALUES ('$nama_pembayar', '$jumlah_bayar','$tgl_bayar') ";

    /*$dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
    while($row = mysqli_fetch_assoc($dana)) {
        $jumlahdana = $row['jumlahdana'];
    }
    $jumlahdana = (int)$jumlahdana;
    $updatedana = $jumlahdana + $jumlah_bayar;
    $putupdatedana = mysqli_query($conn, "UPDATE pagudana SET jumlahdana='$updatedana' WHERE id_pagudana=1");*/

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