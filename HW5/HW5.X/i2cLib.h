/* 
 * File:   i2cLib.h
 * Author: chainatee
 *
 * Created on April 22, 2017, 4:46 PM
 */

#ifndef I2CLIB_H
#define	I2CLIB_H

#define ADDRESS 0x20   // Hardware address

void i2c_master_setup();
void i2c_master_start();
void i2c_master_send(unsigned char byte);
void i2c_master_stop();
void i2c_master_restart();
unsigned char i2c_master_recv(); 
void i2c_master_ack(int val);

void initExpander();
void setExpander(unsigned char address, unsigned char command);
char getExpander(unsigned char address);

#endif	/* I2CLIB_H */

