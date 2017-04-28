---
layout: default
---


{% for post in site.posts %}
<div class="row">
	<div class="small-12 columns">  	
		<a href="{{ site.baseurl }}{{ post.url }}"><h4>{{ post.date | date: '%B %d, %Y' }}<br>{{ post.title }}</h4></a>

	  	{{ post.excerpt }}
	  	<a href="{{ site.baseurl }}{{ post.url }}" class="full-post-link js-pjax">Read more</a>
	</div>
</div>
{% endfor %}


