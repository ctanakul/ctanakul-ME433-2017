#include <proc/p32mx250f128b.h>
#include"i2cLib.h"


void i2c_master_setup(void) {
  ANSELBbits.ANSB2 = 0;
  ANSELBbits.ANSB3 = 0;
  I2C2BRG = 233;            // I2CBRG = [1/(2*Fsck) - PGD]*Pblck - 2  //Fsck = 104ns, PGD = 48MHz
  I2C2CONbits.ON = 1;               // turn on the I2C1 module
}


// Start a transmission on the I2C bus
void i2c_master_start(void) {
    I2C2CONbits.SEN = 1;            // send the start bit
    while(I2C2CONbits.SEN) { ; }    // wait for the start bit to be sent
}

void i2c_master_send(unsigned char byte) { // send a byte to slave
  I2C2TRN = byte;                   // if an address, bit 0 = 0 for write, 1 for read
  while(I2C2STATbits.TRSTAT) { ; }  // wait for the transmission to finish
  if(I2C2STATbits.ACKSTAT) {        // if this is high, slave has not acknowledged
    // ("I2C2 Master: failed to receive ACK\r\n");
  }
}

void i2c_master_stop(void) {          // send a STOP:
  I2C2CONbits.PEN = 1;                // comm is complete and master relinquishes bus
  while(I2C2CONbits.PEN) { ; }        // wait for STOP to complete
}

void i2c_master_restart(void) {     
    I2C2CONbits.RSEN = 1;           // send a restart 
    while(I2C2CONbits.RSEN) { ; }   // wait for the restart to clear
}


unsigned char i2c_master_recv(void) { // receive a byte from the slave
    I2C2CONbits.RCEN = 1; // start receiving data
    while (!I2C2STATbits.RBF) {
        ;
    } // wait to receive the data
    return I2C2RCV; // read and return the data
}

void i2c_master_ack(int val) {        // sends ACK = 0 (slave should send another byte)
                                      // or NACK = 1 (no more bytes requested from slave)
    I2C1CONbits.ACKDT = val;          // store ACK/NACK in ACKDT
    I2C1CONbits.ACKEN = 1;            // send ACKDT
    while(I2C1CONbits.ACKEN) { ; }    // wait for ACK/NACK to be sent
}


void setExpander(unsigned char address, unsigned char command){
    i2c_master_start();
    i2c_master_send((ADDRESS << 1)|0); //opcode = 0b01000000, write mode
    i2c_master_send(address);
    i2c_master_send(command);
    i2c_master_stop();      
}

void initExpander(void){
    i2c_master_setup();
    setExpander(0x00, 0b11110000);
}

char getExpander(unsigned char address){
    i2c_master_start();
    i2c_master_send((ADDRESS << 1)|0);
    i2c_master_send(address); // the register to read from
    i2c_master_restart(); // make the restart bit
    i2c_master_send((ADDRESS << 1)|1); // write the address, shifted left by 1, or with a 1 to indicate reading
    char r = i2c_master_recv(); // save the value returned
    i2c_master_ack(1); // make the ack so the slave knows we got it
    i2c_master_stop(); // make the stop bit
    return r;
}