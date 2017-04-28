---
layout: default
---

## Extraction of data from my Hargassner Pellet Classic Heating

### What is this project about

My heating system has a RS232 port that can be used for monitoring. I am already monitoring the output of 
my eletric meter[^1] so adding more data is just the next step. The RS232 output is not really documented, 
most of the public information about the RS232 port was found on mikrocontroller.net[^2] - but since the 
thread started in 2009 some information is already outdated or has changed with new releases of Hargassner 
heating software versions. 

### What is on the roadmap?

<ul class="fa-ul">
  <li><i class="fa-li fa fa-check-square-o"></i>Building a working serial cable</li>
  <li><i class="fa-li fa fa-check-square-o"></i>Write a binary to read RS232</li>
  <li><i class="fa-li fa fa-check-square-o"></i>Import (some) data to InfluxDb</li>
  <li><i class="fa-li fa fa-square-o"></i>Identify and document fields</li>
  <li><i class="fa-li fa fa-square-o"></i>Document serial cable</li>
  <li><i class="fa-li fa fa-square-o"></i>Publish code</li>
  <li><i class="fa-li fa fa-square-o"></i>Release binary</li>
</ul>

### Some insigths

- [Blog]({{site.baseurl}}/blog)
- [RS232 Output]({{site.baseurl}}/rs232data)

##### Footnotes:

[^1]: https://github.com/adiesner/eHzLogger
[^2]: https://www.mikrocontroller.net/topic/134331