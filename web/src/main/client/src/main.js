/**
 * 
 */

import EventBus from 'vertx3-eventbus-client'
import {getColorForTxValue, getRandomInt} from './util.js'
import Line from './line.js'

//Initialize the context of the canvas
var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");

//Set the canvas width and height to occupy full window
var W = window.innerWidth - 20,
	H = 320;

canvas.width = W;
canvas.height = H;

var lineCount = 20,
	lines = [];

//Create Lines
for (var i=0; i<lineCount; i++)
{
	lines.push(new Line(canvas, i));
}

(function loop() 
{
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	for (var i=0; i < lines.length; i++)
	{
		lines[i].drawDots(ctx);
		lines[i].moveDots();
	}
	
	requestAnimationFrame(loop);
})();

//this is how we connect javascript to the vertx eventbus
var eb = new EventBus('//' + location.host + '/eventbus');

eb.onopen = () => {
	//below raises events to javazone data updates
	eb.registerHandler('javazone.data.updates', (err, msg) => {
		if (!err) 
		{
			var value = 0; //transaction output value container
			var outputs = msg.body.out;
			for (var i = 0; i < outputs.length; i++) 
			{
				value += outputs[i].value;
			}
			
			var w = 10 + Math.round((value) / 10000000);
			if (w > 100) w = 100;
			
			lines[getRandomInt(0, lineCount -1)]
				.pushDot(w, getColorForTxValue(value));
		}
	});
};