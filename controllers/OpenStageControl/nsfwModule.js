const xyValues = new Map()
const rangeValues = new Map()


// this isn't DRY enough, should be able to compress it quite a bit

module.exports = {

    oscInFilter:function(data){
        var {address, args, host, port} = data

        // to recieve one part of an XY value
        if (address.includes('xy') && !(address.includes('touch'))) {
            var xyAddress = address.slice(0,-5)
            var val = [0, 0] // default to 0,0 if key doesn't exist in Map yet

            if(xyValues.has(xyAddress)) {
                var suffix = address.slice(-5)
                var newArg = Math.min(Math.max(args[0].value, 0), 1)
                val = xyValues.get(xyAddress)

                if(suffix.includes('xArg')) { 
                    val[0] = newArg 
                } else if (suffix.includes('yArg')) {
                    val[1] = newArg
                }
            } 

            xyValues.set(xyAddress,val)
            receive(xyAddress, val[0], val[1])
            return // bypass original message
        }

        // to recieve one part of an range value
        if (address.includes('range') && !(address.includes('touch'))) {
            var rangeAddress = address.slice(0,-6)
            var val = [0, 0] // default to 0,0 if key doesn't exist in Map yet

            if(rangeValues.has(rangeAddress)) {
                var suffix = address.slice(-6)
                var newArg = Math.min(Math.max(args[0].value, 0), 1)
                val = rangeValues.get(rangeAddress)

                if(suffix.includes('loArg')) { 
                    val[0] = newArg 
                } else if (suffix.includes('hiArg')) {
                    val[1] = newArg
                }
            } 

            rangeValues.set(rangeAddress, val)
            receive(rangeAddress, val[0], val[1])
            return // bypass original message
        }

        return {address, args, host, port}
    },

    oscOutFilter:function(data){
        var {address, args, host, port, clientId} = data

        // for sending XY messages separately
        if (address.includes('xy') && !(address.includes('touch'))) {

            xyValues.set(address, [args[0].value, args[1].value])

            send(host, port, address + '_xArg', xyValues.get(address)[0])
            send(host, port, address + '_yArg', xyValues.get(address)[1])
            return // bypass original message 
        }

        // for sending range messages separately
        if (address.includes('range') && !(address.includes('touch'))) {

            rangeValues.set(address, [args[0].value, args[1].value])

            send(host, port, address + '_loArg', rangeValues.get(address)[0])
            send(host, port, address + '_hiArg', rangeValues.get(address)[1])
            return // bypass original message 
        } 

        return {address, args, host, port}
    }
}
