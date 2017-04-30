#include<xc.h>           // processor SFR definitions
#include<sys/attribs.h>  // __ISR macro
#include"i2cLib.h"      // SPI 
#include"ILI9163C.h"
#include<math.h>
#include<stdio.h>

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
    SPI1_init();    
    LCD_init();
    initIMU();
    
    __builtin_enable_interrupts(); 
    char msg[100];
    LCD_clearScreen(BG_COLOR);
//    sprintf(msg, "%d", getExpander(0x0F));
//    draw_string(10, 60, msg, BLACK, BG_COLOR);
    int arrLen = 14;
    unsigned char data[arrLen];
    signed short temp, x_ang,y_ang, z_ang, x_l_acc, y_l_acc, z_l_acc;
    
    while(1) {
        I2C_read_multiple_hack(ADDRESS, OUT_TEMP_L, data, arrLen);
        temp = (data[1] << 8) | data[0];
        x_ang = (data[3] << 8) | data[2];
        y_ang = (data[5] << 8) | data[4];
        z_ang = (data[7] << 8) | data[6];
        x_l_acc = (data[9] << 8) | data[8];
        y_l_acc = (data[11] << 8) | data[10];
        z_l_acc = (data[13] << 8) | data[12];
        
        draw_bar_accel(x_l_acc*0.00061, y_l_acc*0.00061, 3, RED, BG_COLOR);
        
//        sprintf(msg, "%.2f  ", x_l_acc*0.00061);
//        draw_string(10, 10, msg, YELLOW, BLACK); 
//        sprintf(msg, "%.2f  ", y_l_acc*0.00061);
//        draw_string(10, 10, msg, YELLOW, BLACK); 
//        sprintf(msg, "%.2f  ", z_l_acc*0.00061);
//        draw_string(10, 30, msg, YELLOW, BLACK); 

//        draw_bar_x_accel(accel, 5, RED, BG_COLOR);
    }
}