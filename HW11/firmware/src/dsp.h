/* 
 * File:   dsp.h
 * Author: chainatee
 *
 * Created on April 22, 2017, 4:46 PM
 */

#ifndef DSP_H
#define	DSP_H

//MAF
float maf(float data[], int size);
float iir(float data[], float old_avg);
float fir(float data[], int size);
#endif	/* I2CLIB_H */

