<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];

if ( $key == "update" ){

    $id_aset       = $_POST['id_aset'];
    $nama_aset     = $_POST['nama_aset'];
    $harga_aset    = $_POST['harga_aset'];
    $tgl_aset      = $_POST['tgl_aset'];
    $bukti         = $_POST['bukti'];

    $harga_aset = (int)$harga_aset;
    $tgl_aset =  date('Y-m-d', strtotime($tgl_aset));

    $query = "UPDATE aset SET 
    nama_aset='$nama_aset', 
    harga_aset='$harga_aset', 
    tgl_aset='$tgl_aset'
    WHERE id_aset='$id_aset' ";


    //mengupdate data di pagudana

    $aset = mysqli_query($conn, "SELECT harga_aset FROM aset WHERE id_aset='$id_aset'");
    while($row1 = mysqli_fetch_assoc($aset)) {
        $harga_aset1 = $row1['harga_aset'];
    }

    $dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
    while($row2 = mysqli_fetch_assoc($dana)) {
        $jumlahdana = $row2['jumlahdana'];
    }
    $jumlahdana = (int)$jumlahdana;
    $updatedana = $jumlahdana - ($harga_aset - $harga_aset1);
    $putupdatedana = mysqli_query($conn, "UPDATE pagudana SET jumlahdana='$updatedana' WHERE id_pagudana=1");
    

        if ( mysqli_query($conn, $query) ){

            if ($bukti == null) {

                $result["value"] = "1";
                $result["message"] = "Success";
    
                echo json_encode($result);
                mysqli_close($conn);

            } else {

                $path = "notaasetpict/$id_aset.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE aset SET bukti='$finalPath' WHERE id_aset='$id_aset' ";
            
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

                }else {
                        
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