-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 02, 2020 at 08:40 AM
-- Server version: 10.1.38-MariaDB
-- PHP Version: 7.3.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `dompetukm`
--

-- --------------------------------------------------------

--
-- Table structure for table `aset`
--

CREATE TABLE `aset` (
  `id_aset` int(11) NOT NULL,
  `nama_aset` varchar(50) NOT NULL,
  `harga_aset` int(50) NOT NULL,
  `tgl_aset` date NOT NULL,
  `bukti` varchar(100) NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `aset`
--

INSERT INTO `aset` (`id_aset`, `nama_aset`, `harga_aset`, `tgl_aset`, `bukti`, `id_user`) VALUES
(1, 'PrinterEpson', 1200000, '2020-08-24', '/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDABALDA4MChAODQ4SERATGCgaGBYWGDEjJR0oOjM9PDkz\nODdASFxOQERXRTc4UG1RV19', 0);

-- --------------------------------------------------------

--
-- Table structure for table `kas`
--

CREATE TABLE `kas` (
  `id_kas` int(11) NOT NULL,
  `nama_pembayar` varchar(50) NOT NULL,
  `jumlah_bayar` int(50) NOT NULL,
  `tgl_bayar` date NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `kas`
--

INSERT INTO `kas` (`id_kas`, `nama_pembayar`, `jumlah_bayar`, `tgl_bayar`, `id_user`) VALUES
(1, 'Irfan', 100000, '2020-08-17', 0);

-- --------------------------------------------------------

--
-- Table structure for table `kegiatan`
--

CREATE TABLE `kegiatan` (
  `id_kegiatan` int(11) NOT NULL,
  `nama_kegiatan` varchar(30) NOT NULL,
  `tgl_kegiatan` date NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `kegiatan`
--

INSERT INTO `kegiatan` (`id_kegiatan`, `nama_kegiatan`, `tgl_kegiatan`, `id_user`) VALUES
(1, 'Seminar Kewirausahaan', '2020-08-24', 0);

-- --------------------------------------------------------

--
-- Table structure for table `pagudana`
--

CREATE TABLE `pagudana` (
  `id_pagudana` int(11) UNSIGNED NOT NULL,
  `jumlahdana` int(30) NOT NULL,
  `tgl_diterima` date NOT NULL,
  `bukti` varchar(100) NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `pagudana`
--

INSERT INTO `pagudana` (`id_pagudana`, `jumlahdana`, `tgl_diterima`, `bukti`, `id_user`) VALUES
(1, 10680000, '2020-06-16', '/ukmbudget/buktipagudanapict/1.jpeg', 0);

-- --------------------------------------------------------

--
-- Table structure for table `pagudanastatic`
--

CREATE TABLE `pagudanastatic` (
  `id_pagudanastc` int(11) NOT NULL,
  `jumlahdana` int(50) NOT NULL,
  `id_pagudana` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `pagudanastatic`
--

INSERT INTO `pagudanastatic` (`id_pagudanastc`, `jumlahdana`, `id_pagudana`) VALUES
(1, 12000000, 1);

-- --------------------------------------------------------

--
-- Table structure for table `pengeluarankegiatan`
--

CREATE TABLE `pengeluarankegiatan` (
  `id_pengeluaran` int(11) NOT NULL,
  `nama_pengeluaran` varchar(50) NOT NULL,
  `jumlah_item` int(10) NOT NULL,
  `harga_satuan` int(50) NOT NULL,
  `total_harga` int(50) NOT NULL,
  `tgl_pengeluaran` date NOT NULL,
  `bukti` varchar(100) NOT NULL,
  `id_kegiatan` int(11) NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `pengeluarankegiatan`
--

INSERT INTO `pengeluarankegiatan` (`id_pengeluaran`, `nama_pengeluaran`, `jumlah_item`, `harga_satuan`, `total_harga`, `tgl_pengeluaran`, `bukti`, `id_kegiatan`, `id_user`) VALUES
(1, 'Makanan Ringan', 60, 2000, 120000, '2020-08-25', '/ukmbudget/notapict/1.jpeg', 1, 0);

--
-- Triggers `pengeluarankegiatan`
--
DELIMITER $$
CREATE TRIGGER `total_hargaup` BEFORE UPDATE ON `pengeluarankegiatan` FOR EACH ROW BEGIN
    SET NEW.total_harga = NEW.jumlah_item * NEW.harga_satuan; 
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `totalharga` BEFORE INSERT ON `pengeluarankegiatan` FOR EACH ROW BEGIN
    SET NEW.total_harga = NEW.jumlah_item * NEW.harga_satuan;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `tbl_user`
--

CREATE TABLE `tbl_user` (
  `id` int(11) NOT NULL,
  `unique_id` varchar(23) NOT NULL,
  `nama` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `encrypted_password` varchar(256) NOT NULL,
  `salt` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tbl_user`
--

INSERT INTO `tbl_user` (`id`, `unique_id`, `nama`, `email`, `encrypted_password`, `salt`) VALUES
(1, '', 'Irfan Fauzan', 'fauzanirf213', 'oySiYnVZDZd5H08nxrOyaL7J1+s0NDQyYmFjMmNh', '4442bac2ca');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `aset`
--
ALTER TABLE `aset`
  ADD PRIMARY KEY (`id_aset`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `kas`
--
ALTER TABLE `kas`
  ADD PRIMARY KEY (`id_kas`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `kegiatan`
--
ALTER TABLE `kegiatan`
  ADD PRIMARY KEY (`id_kegiatan`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `pagudana`
--
ALTER TABLE `pagudana`
  ADD PRIMARY KEY (`id_pagudana`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `pagudanastatic`
--
ALTER TABLE `pagudanastatic`
  ADD PRIMARY KEY (`id_pagudanastc`),
  ADD KEY `id_pagudana` (`id_pagudana`);

--
-- Indexes for table `pengeluarankegiatan`
--
ALTER TABLE `pengeluarankegiatan`
  ADD PRIMARY KEY (`id_pengeluaran`),
  ADD KEY `id_kegiatan` (`id_kegiatan`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `tbl_user`
--
ALTER TABLE `tbl_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_id` (`unique_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `aset`
--
ALTER TABLE `aset`
  MODIFY `id_aset` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `kas`
--
ALTER TABLE `kas`
  MODIFY `id_kas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `kegiatan`
--
ALTER TABLE `kegiatan`
  MODIFY `id_kegiatan` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `pagudana`
--
ALTER TABLE `pagudana`
  MODIFY `id_pagudana` int(11) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `pagudanastatic`
--
ALTER TABLE `pagudanastatic`
  MODIFY `id_pagudanastc` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `pengeluarankegiatan`
--
ALTER TABLE `pengeluarankegiatan`
  MODIFY `id_pengeluaran` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `tbl_user`
--
ALTER TABLE `tbl_user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
