/* 
	WebStats version 1.2
	https://github.com/Dantevg/WebStats
	
	by RedPolygon
*/

class WebStats {
	static updateInterval = 10000
	
	constructor({table, ip, port, sortBy, descending}){
		this.display = new Display(table, sortBy, descending)
		this.connection = new Connection(ip, port)
		
		// Set online status update interval
		if(WebStats.updateInterval > 0) this.startUpdateInterval(true)
		document.addEventListener("visibilitychange", () => document.hidden
			? this.stopUpdateInterval() : this.startUpdateInterval())
		
		// Get data and init
		this.connection.getStats().then(data => this.init(data))
	}
	
	init(data){
		this.display.init(data) // Display data in table
		
		// Get sorting from url params, if present
		const params = (new URL(document.location)).searchParams
		let sortBy = params.get("sort") ?? this.display.sortBy
		let order = params.get("order")
		let descending = order ? order.startsWith("d") : this.display.descending
		this.display.sort(sortBy, descending)
	}
	
	update(){
		this.connection.getStats().then(this.display.updateStats.bind(this.display))
	}
	
	startUpdateInterval(first){
		this.interval = setInterval(this.update.bind(this), WebStats.updateInterval)
		if(!first) this.update()
	}
	
	stopUpdateInterval(){
		clearInterval(this.interval)
	}
	
}