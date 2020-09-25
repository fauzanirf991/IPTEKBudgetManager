<?php
require_once 'connect.php';

$key = $_POST['key'];

$nama_aset     = $_POST['nama_aset'];
$harga_aset    = $_POST['harga_aset'];
$tgl_aset      = $_POST['tgl_aset'];
$bukti    = $_POST['bukti'];

if ( $key == "insert" ){
    $harga_aset = (int)$harga_aset;
    $tgl_aset = date('Y-m-d', strtotime($tgl_aset));

    

    $query = "INSERT INTO aset (nama_aset, harga_aset, tgl_aset, bukti)
    VALUES ('$nama_aset', '$harga_aset', '$tgl_aset', '$bukti') ";
    

        if ( mysqli_query($conn, $query)){

            $dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
            while($row = mysqli_fetch_assoc($dana)) {
            $jumlahdana = $row['jumlahdana'];
            }
            $jumlahdana = (int)$jumlahdana;
            $updatedana = $jumlahdana - $harga_aset;
            $putupdatedana = mysqli_query($conn, "UPDATE pagudana SET jumlahdana='$updatedana' WHERE id_pagudana=1");

            
                $id = mysqli_insert_id($conn);
                $path = "notaasetpict/$id.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE aset SET bukti='$finalPath' WHERE id_aset='$id' ";
            
                if (mysqli_query($conn, $insert_picture)) {
            
                    if ( file_put_contents( $path, base64_decode($bukti) ) ) {
                        
                        $result["value"] = "1";
                        $result["message"] = "Success!";
            
                        echo json_encode($result);
                        mysqli_close($conn);
            
                    } else {
                        
                        $response["value"] = "0";
                        $response["message"] = "Error! ".mysqli_error($conn);
                        echo json_encode($response);
 
                        mysqli_close($conn);
                    }

                }
            

        } 
        else {
            $response["value"] = "0";
            $response["message"] = "Error! ".mysqli_error($conn);
            echo json_encode($response);

            mysqli_close($conn);
        }
}



?>