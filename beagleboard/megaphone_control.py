import csv
import socket, errno
import OSC
from OSC import OSCClient, OSCMessage
import random, time, threading
from threading import Timer
import os
# Q: why so slow to startup?

# Megaphone: P9
# servo GND  01  02 GND mega
#	    n/a  03  04 n/a
# servo	+5v  05  06 +5v mega
#       n/a  07  08 n/a
#       n/a  09  10 n/a
# servo	CTRL 11  12	PITCH_UP mega	
#  mega REC	 12  13 PITCH_DN mega
#  mega PLAY 15  16	n/a
#  mega VOL  17  18	n/a

# device spec:
# [0] the operationName is used as part of the oscPath the handler will responder to
# [1] the type is refers to actuation or sensing
# [2] the pinMode is used to determine which part of the BBIO library python will use
# [3] the pin is the specific pinout that the message will be sent to

# comment these out if running locally:

try:
    import Adafruit_BBIO.GPIO as GPIO
    import Adafruit_BBIO.PWM as PWM
except ImportError:
    print "Adafruit BBIO library not found...(not a problem if running in local mode)"

receiveAddress = '127.0.0.1', 9991 # 10000 sometimes already in use, why?
pythonOscPath = '/megaphone'
pythonSetupHandlerOscPath = '/setupHandler'
initialisedPins = []

# initialisation

def parse_command_line_options():
	global bbbExists
	# for info on use see: http://docs.python.org/2/library/optparse.html
	from optparse import OptionParser
	parser = OptionParser()
	parser.add_option("-n", "--noBBB", action="store_false", default = True, dest="bbbExists", help="ignore BBIO commands if testing without BeagleBone Black")
	(options, args) = parser.parse_args()
	bbbExists = options.bbbExists
	if bbbExists == True:
		print "Assuming BeagleBone Black is present...please ensure it is connected"
	else:
		print "Assuming BeagleBone Black is not present...will simulate and print values for testing"

def init_server():
	print "\nStarting OSCServer. Use ctrl-C to quit."
	global pythonServer
	pythonServer = OSC.OSCServer(receiveAddress)
	global st
	pythonServer.addDefaultHandlers() # registers 'default' handler (for unmatched messages + more)
	st = threading.Thread( target = pythonServer.serve_forever )
	st.start()

def init_device():
    deviceOperations = [
        ['position', 'PWM', 'P9_16', 60 ],
        ['record', 'GPIO', 'P9_27', None ],
        ['play', 'GPIO', 'P9_12', None ],
        ['playVolume', 'PWM', 'P9_22', 20000 ]
        ]
    for operation in deviceOperations:
        operationName = operation[0]
        pinMode = operation[1]
        pin = operation[2]
	param = operation[3]
        init_actuation_operation(operationName, pinMode, pin, param)

# actuation functions:
def init_actuation_operation(operationName, pinMode, pin, param):
	if pinMode == 'GPIO':
		init_gpio_pin(operationName, pin)
	elif pinMode == 'PWM':
	 	init_pwm_pin(operationName, pin, param)

def init_gpio_pin(operationName, pin):
    recvOscPath = pythonOscPath + '/' + operationName
    print recvOscPath + " " + pin

    def gpio_pin_handler(addr, tags, msg, source):  # how can we pass in our own values here?
        value = int(msg[0]) # value should always be a single value, so we just take the first in the array
        pinIsInitialised = pin in initialisedPins
        if not pinIsInitialised:
            print "initialising GPIO '{0}'".format(pin)
            initialisedPins.append(pin)
            if bbbExists == True:
                GPIO.setup(pin, GPIO.OUT)
        print "setting GPIO '{0}' '{1}'".format(pin, value)
        if bbbExists == True:
            GPIO.output(pin, value) # what if the value provided is not an integer, should we enforce this here?

    pythonServer.addMsgHandler(recvOscPath, gpio_pin_handler)
            
def init_pwm_pin(operationName, pin, param):
    recvOscPath = pythonOscPath + '/' + operationName
    print recvOscPath + " " + pin

    def pwm_pin_handler(addr, tags, msg, source): # how can we pass in our own values here?
        value = msg[0] # value should always be a single value, so we just take the first in the array
        if operationName == 'position':
            value = scale(value, (0., 180.), (4., 13.)) # convert degrees to values the servo understands 
            print"remapped position to '{0}'".format(value)
        if operationName == 'playVolume':
            value = scale(value, (0., 1.), (0., 100.)) # preferably remap more exponetially here
            print"remapped volume to '{0}'".format(value)
        pinIsInitialised = pin in initialisedPins
        # print initialisedPins
        if not pinIsInitialised:
            	print "starting PWM '{0}' '{1}'".format(pin, value, param)
            	initialisedPins.append(pin)
           	if bbbExists == True:
			PWM.start(pin, value, param)
        else:
            print "setting PWM '{0} '{1}'".format(pin, value)
            if bbbExists == True:
                PWM.set_duty_cycle(pin, value)

    pythonServer.addMsgHandler(recvOscPath, pwm_pin_handler)

def scale(val, src, dst):
    return ((val - src[0]) / (src[1]-src[0])) * (dst[1]-dst[0]) + dst[0]
    
# main:
parse_command_line_options()
init_server()
init_device()

try: 
     while True: 
         time.sleep(1) 

except KeyboardInterrupt:
    print "\nClosing OSCServer."
    pythonServer.close()
    print "Waiting for Server-thread to finish"
    st.join() ##!!!
    print "Done"
