#include <proc/p32mx250f128b.h>
#include"spiLib.h"

//#define CS LATBbits.LATB11   // Chip Select Pin B3

void initSPI1() {   
    // set function to RP pin

    RPB8Rbits.RPB8R = 0b0011;       // SDO1 pin RPB8 (pin 16)
    SDI1Rbits.SDI1R = 0b0001;       // SDI1 pin RPB5 (pin 14)
    TRISBbits.TRISB7 = 0;    
    CS = 1;                         // manual chip select (pin 11)

    SPI1CON = 0;                    // set all value to be 0
    SPI1BUF;                        // clear the rx buffer by reading from it
    SPI1STATbits.SPIROV = 0;         // clear the over flow bit
    SPI1CONbits.MODE16 = 0;         // 8-bit data transfer
    SPI1CONbits.MODE32 = 0;         // 8-bit data transfer
    SPI1CONbits.CKP = 0;            
    SPI1CONbits.CKE = 1;   
    SPI1CONbits.SSEN = 0;
    SPI1CONbits.MSTEN = 1;          // set SPI of PIC32 as a master
    SPI1BRG = 1;                    // clock frequency or Baud Rate (the more slow it is. the more readability of nScope is))
    SPI1CONbits.ON = 1;             // enable SPI peripheral

}


unsigned char spi_io(unsigned char buf){
// write and read buffer to spi
    SPI1BUF = buf; //first 8 bits
    while(!SPI1STATbits.SPIRBF){ //wait for the buffered to be read
        ;
    }
    return SPI1BUF;//read 8 bits
}


void setVoltage(unsigned char channel, unsigned char voltage){
    unsigned char bit1 = channel << 3 | 0b0111;
    bit1 = bit1 << 4 | voltage >> 4;
    char bit2 = voltage << 4;
    CS = 0;
    spi_io(bit1);
    spi_io(bit2);
    CS = 1;
}
