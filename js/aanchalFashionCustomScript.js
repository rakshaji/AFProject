$(document).ready(function() {
	showBookmarks();
});

function showBookmarks() {
	var items = [], options = [], values = [];
	var count = 0;

	//Iterate all td's in second column
	$('td:nth-child(2)').each(function() {
		//add item to array
		var bookmarkName = $(this).text();
		if (bookmarkName != "") {
			let id = "bookmark" + (count++);
			$(this).attr('id', id);
			items.push(bookmarkName);
			values.push(id);
		}
	});

	//restrict array to unique items
	//var items = $.unique( items );

	//iterate unique array and build array of select options
	$.each(items, function(i, item) {
		options.push('<option value="' + values[i] + '">' + item + '</option>');
	})

	// finally empty the select and append the items from the array
	$('#bookmarkSelect').empty().prepend(options.join());

	// to scroll to a bookmark location
	$("#bookmarkSelect").on('change', function() {
		var selectedVal = $('select#bookmarkSelect option:selected').val();
		// document.getElementById(selectedVal).scrollIntoView({ behavior: 'smooth' });
		scrollToTargetAdjusted(selectedVal);
	});

}

function scrollToTargetAdjusted(id) {
	var element = document.getElementById(id);
	var headerOffset = 45;
	var elementPosition = element.getBoundingClientRect().top;
	var offsetPosition = elementPosition + window.pageYOffset - headerOffset;

	window.scrollTo({
		top: offsetPosition,
		behavior: "instant"
	});
}

