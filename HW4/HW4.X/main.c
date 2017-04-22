#include<xc.h>           // processor SFR definitions
#include<sys/attribs.h>  // __ISR macro
#include"spiLib.h"      // SPI 
#include<math.h>

// DEVCFG0
#pragma config DEBUG = OFF // no debugging
#pragma config JTAGEN = OFF // no jtag
#pragma config ICESEL = ICS_PGx1 // use PGED1 and PGEC1
#pragma config PWP = OFF // no write protect
#pragma config BWP = OFF // no boot write protect
#pragma config CP = OFF // no code protect

// DEVCFG1
#pragma config FNOSC = PRIPLL // use primary oscillator with pll
#pragma config FSOSCEN = OFF // turn off secondary oscillator
#pragma config IESO = OFF // no switching clocks
#pragma config POSCMOD = HS // high speed crystal mode
#pragma config OSCIOFNC = OFF // disable secondary osc
#pragma config FPBDIV = DIV_1 // divide sysclk freq by 1 for peripheral bus clock
#pragma config FCKSM = CSDCMD // do not enable clock switch
#pragma config WDTPS = PS1 // use slowest wdt
#pragma config WINDIS = OFF // wdt no window mode
#pragma config FWDTEN = OFF // wdt disabled
#pragma config FWDTWINSZ = WINSZ_25 // wdt window at 25%

// DEVCFG2 - get the sysclk clock to 48MHz from the 8MHz crystal
#pragma config FPLLIDIV = DIV_2 // divide input clock to be in range 4-5MHz
#pragma config FPLLMUL = MUL_24 // multiply clock after FPLLIDIV
#pragma config FPLLODIV = DIV_2 // divide clock after FPLLMUL to get 48MHz
#pragma config UPLLIDIV = DIV_2 // divider for the 8MHz input clock, then multiplied by 12 to get 48MHz for USB
#pragma config UPLLEN = ON // USB clock on

// DEVCFG3
#pragma config USERID = 0 // some 16bit userid, doesn't matter what
#pragma config PMDL1WAY = OFF // allow multiple reconfigurations
#pragma config IOL1WAY = OFF // allow multiple reconfigurations
#pragma config FUSBIDIO = ON // USB pins controlled by USB module
#pragma config FVBUSONIO = ON // USB BUSON controlled by USB module



unsigned int sinWave[100];
unsigned int triWave[200];
void createWaveformArray(){
    unsigned int i = 0;
    for (i = 0; i < 100; i++) {
        sinWave[i] = ((sin(i*2*M_PI/100) + 1)*255/2);
    }
    for (i = 0; i < 200; i++) {
        triWave[i] = (255*i/200);
    }    
}




int main() {

    __builtin_disable_interrupts();

    // set the CP0 CONFIG register to indicate that kseg0 is cacheable (0x3)
    __builtin_mtc0(_CP0_CONFIG, _CP0_CONFIG_SELECT, 0xa4210583);

    // 0 data RAM access wait states
    BMXCONbits.BMXWSDRM = 0x0;

    // enable multi vector interrupts
    INTCONbits.MVEC = 0x1;

    // disable JTAG to get pins back
    DDPCONbits.JTAGEN = 0;

    initSPI1();    

    createWaveformArray();

    
    __builtin_enable_interrupts();

    
    unsigned int counter1 = 0; //counter counting to 1000
    unsigned int counter2 = 0;
    
    while(1) {
        _CP0_SET_COUNT(0);
//        while(_CP0_GET_COUNT() < 48000000){
        while(_CP0_GET_COUNT() < 48000000/2/1000){
            ;   //wait to make this loop runs at 1kHz, the PIC32 SYSCLK is 48 MHz and USBCLK is half of that
        }
        // Generate sinusoidal plot
        setVoltage(0, sinWave[counter1]);
        // Generate triangle plot
        setVoltage(1, triWave[counter2]);        
//        setVoltage(1, 100);
//        setVoltage(0, 255);
        counter1++;
        counter2++;
        if(counter1 == 100){
            counter1 = 0;
        }
        if(counter2 == 200){
            counter2 = 0;
        }
        //        }

    }
//    return 0;
}