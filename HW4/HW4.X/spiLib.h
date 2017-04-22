/* 
 * File:   spiLib.h
 * Author: chainatee
 *
 * Created on April 16, 2017, 1:46 PM
 */

#ifndef SPILIB_H
#define	SPILIB_H

#define CS LATBbits.LATB7   // Chip Select Pin B3
void initSPI1();
unsigned char spi_io(unsigned char buf);

#endif	/* SPILIB_H */

