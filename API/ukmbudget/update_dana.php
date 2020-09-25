<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];

if ( $key == "update" ){

    $id_pagudana        = $_POST['id_pagudana'];
    $jumlahdana     = $_POST['jumlah_dana'];
    $tgl_diterima   = $_POST['tgl_diterima'];
    $bukti          = $_POST['bukti'];

    $jumlahdana = (int)$jumlahdana;
    $tgl_diterima =  date('Y-m-d', strtotime($tgl_diterima));

    $query = "UPDATE pagudana SET 
    jumlahdana='$jumlahdana',  
    tgl_diterima='$tgl_diterima',
    bukti='$bukti'
    WHERE id_pagudana=$id_pagudana ";

    $query2 = mysqli_query($conn, "UPDATE pagudanastatic SET jumlahdana='$jumlahdana',
                                    WHERE id_pagudanastc=1");

        if ( mysqli_query($conn, $query) ){

            if ($bukti == null) {

                $result["value"] = "1";
                $result["message"] = "Success";
    
                echo json_encode($result);
                mysqli_close($conn);

            } else {

                $path = "buktipagudanapict/$id_pagudana.jpeg";
                $finalPath = "/ukmbudget/".$path;

                $insert_picture = "UPDATE pagudana SET bukti='$finalPath' WHERE id_pagudana='$id_pagudana' ";
            
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